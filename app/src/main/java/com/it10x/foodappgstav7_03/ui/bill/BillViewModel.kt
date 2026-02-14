package com.it10x.foodappgstav7_03.ui.bill

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_03.data.PrinterRole
import com.it10x.foodappgstav7_03.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_03.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_03.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_03.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_03.data.pos.dao.OutletDao
import com.it10x.foodappgstav7_03.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosOrderPaymentEntity
import com.it10x.foodappgstav7_03.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_03.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_03.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_03.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_03.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_03.printer.PrinterManager
import com.it10x.foodappgstav7_03.printer.ReceiptFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.it10x.foodappgstav7_03.data.print.OutletInfo
import com.it10x.foodappgstav7_03.data.print.OutletMapper
import com.it10x.foodappgstav7_03.ui.payment.PaymentInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import com.it10x.foodappgstav7_03.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_03.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerLedgerEntity
class BillViewModel(
    private val kotItemDao: KotItemDao,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val orderSequenceRepository: OrderSequenceRepository,
    private val outletDao: OutletDao,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager,
    private val outletRepository: OutletRepository,
    private val paymentRepository: POSPaymentRepository,
    private val customerDao: PosCustomerDao,
    private val ledgerDao: PosCustomerLedgerDao
) : ViewModel() {

    // --------------------------------------------------------
    // UI State + Delivery Address
    // --------------------------------------------------------
    private val _deliveryAddress = MutableStateFlow<DeliveryAddressUiState?>(null)

    private val _loading = MutableStateFlow(false)
    val deliveryAddress: DeliveryAddressUiState? get() = _deliveryAddress.value

    private val _uiState = MutableStateFlow(BillUiState(loading = true))
    val uiState: StateFlow<BillUiState> = _uiState

    private val _currencySymbol = MutableStateFlow("₹") // fallback
    val currencySymbol: StateFlow<String> = _currencySymbol

    private val _discountFlat = MutableStateFlow(0.0)
    private val _discountPercent = MutableStateFlow(0.0)





    fun setFlatDiscount(value: Double) {
        _discountFlat.value = value.coerceAtLeast(0.0)
        _discountPercent.value = 0.0 // reset percent
    }

    fun setPercentDiscount(value: Double) {
        _discountPercent.value = value.coerceAtLeast(0.0)
        _discountFlat.value = 0.0 // reset flat
    }

  //  val outletInfo: StateFlow<OutletInfo> = outletRepository.outletInfo
    // ✅ Expose orderType safely for Compose UI
    val orderTypePublic: String
        get() = orderType

    init {
        Log.d("BILL_INIT", "Initialized | table=$tableId")
        observeBill()
        loadCurrency()
    }

    // --------------------------------------------------------
    // Observe Bill (Live billing snapshot)
    // --------------------------------------------------------

    private fun observeBill() {
        viewModelScope.launch {
            combine(
                kotItemDao.getItemsForTable(tableId),
                _discountFlat,
                _discountPercent
            ) { kotItems, flat, percent ->
                Triple(kotItems, flat, percent)
            }.collectLatest { (kotItems, flat, percent) ->

                val doneItems = kotItems.filter { it.status == "DONE" }

                val billingItems = doneItems
                    .groupBy {
                        listOf(
                            it.productId,
                            it.basePrice,
                            it.taxRate,
                            it.note,
                            it.modifiersJson
                        )
                    }
                    .map { (_, group) ->

                        val first = group.first()
                        val quantity = group.sumOf { it.quantity }
                        val itemTotal = first.basePrice * quantity

                        val taxTotal = group.sumOf {
                            if (it.taxType == "exclusive")
                                it.basePrice * it.quantity * (it.taxRate / 100)
                            else 0.0
                        }

                        BillingItemUi(
                            id = first.id,
                            productId = first.productId,
                            name = first.name,
                            basePrice = first.basePrice,
                            taxRate = first.taxRate,
                            quantity = quantity,
                            finalTotal = itemTotal + taxTotal,
                            itemtotal = itemTotal,
                            taxTotal = taxTotal,
                            note = first.note ?: "",
                            modifiersJson = first.modifiersJson ?: ""
                        )

                    }



                val subtotal = billingItems.sumOf { it.itemtotal }
                val tax = billingItems.sumOf { it.taxTotal }

                val percentValue = subtotal * (percent / 100.0)
                val appliedDiscount = if (flat > 0) flat else percentValue

                val finalTotal = (subtotal + tax - appliedDiscount)
                    .coerceAtLeast(0.0)

                _uiState.value = BillUiState(
                    loading = false,
                    items = billingItems,
                    subtotal = subtotal,
                    tax = tax,
                    discountFlat = flat,
                    discountPercent = percent,
                    discountApplied = appliedDiscount,
                    total = finalTotal
                )
            }
        }
    }


    private fun loadCurrency() {
        viewModelScope.launch {
            val outletInfo = outletRepository.getOutletInfo()
            _currencySymbol.value = outletInfo.defaultCurrency
        }
    }



    suspend fun hasPendingKitchenItems(): Boolean {
        return kotItemDao.countKitchenPending(tableId) > 0
    }
    // --------------------------------------------------------
    // Payment + Order Creation
    // --------------------------------------------------------
    fun payBill(payments: List<PaymentInput>) {
        viewModelScope.launch {

            val kotItems = kotItemDao
                .getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }

            if (kotItems.isEmpty()) return@launch

            val itemSubtotal = kotItems.sumOf { it.basePrice * it.quantity }

            val taxTotal = kotItems.sumOf {
                if (it.taxType == "exclusive")
                    it.basePrice * it.quantity * (it.taxRate / 100)
                else 0.0
            }

            val now = System.currentTimeMillis()
            val orderId = UUID.randomUUID().toString()

            val outlet = outletDao.getOutlet()
                ?: error("Outlet not configured")

            val srno = orderSequenceRepository.nextOrderNo(
                outletId = outlet.outletId,
                businessDate = SimpleDateFormat(
                    "yyyyMMdd",
                    Locale.getDefault()
                ).format(Date())
            )

            val flat = _discountFlat.value
            val percent = _discountPercent.value
            val percentValue = itemSubtotal * (percent / 100.0)

            val discount = if (flat > 0) flat else percentValue

            val grandTotal = (itemSubtotal + taxTotal - discount)
                .coerceAtLeast(0.0)

            // =====================================================
            // ✅ PAYMENT CALCULATION (NEW LOGIC)
            // =====================================================

            val totalPaid = payments
                .filter { it.mode != "CREDIT" }
                .sumOf { it.amount }

            val totalCredit = payments
                .filter { it.mode == "CREDIT" }
                .sumOf { it.amount }

            val dueAmount = (grandTotal - totalPaid).coerceAtLeast(0.0)


            val paymentStatus = when {
                totalPaid == 0.0 && totalCredit > 0 -> "CREDIT"
                dueAmount > 0 -> "PARTIAL"
                else -> "PAID"
            }


            val phone = deliveryAddress?.phone?.trim().orEmpty()

            Log.d("DEBUG_PHONE", "deliveryAddress=$deliveryAddress")
            Log.d("DEBUG_PHONE", "phone=${deliveryAddress?.phone}")
            if (paymentStatus == "CREDIT" && phone.isBlank()) {
                Log.e("DEBUG_PHONE", "Phone required for credit sale")
                return@launch
            }


// =====================================================
// CUSTOMER CREDIT HANDLING
// =====================================================

            var resolvedCustomerId: String? = null

            if (paymentStatus == "CREDIT" || paymentStatus == "PARTIAL") {

                val phone = deliveryAddress?.phone?.trim()

                if (phone.isNullOrBlank()) {
                    Log.e("CREDIT", "Phone required")
                    return@launch
                }


                resolvedCustomerId = phone

                val existingCustomer = customerDao.getCustomerById(phone)

                if (existingCustomer == null) {

                    // 🔥 FAST POS → create minimal customer
                    val customer = PosCustomerEntity(
                        id = phone,
                        ownerId = outlet.ownerId,
                        outletId = outlet.outletId,

                        name = deliveryAddress?.name ?: "Customer",
                        phone = phone,

                        addressLine1 = null,
                        addressLine2 = null,
                        city = null,
                        state = null,
                        zipcode = null,
                        landmark = null,

                        creditLimit = 0.0,
                        currentDue = dueAmount, // ✅ SET DUE DIRECTLY

                        createdAt = now,
                        updatedAt = null
                    )

                    customerDao.insert(customer)

                } else {
                    // ✅ Increase due
                  //  customerDao.increaseDue(phone, dueAmount)
                    customerDao.increaseDue(phone, totalCredit)
                }

                // 🔥 Ledger Entry
                val lastBalance = ledgerDao.getLastBalance(phone) ?: 0.0
                val newBalance = lastBalance + totalCredit

                val ledgerEntry = PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = outlet.ownerId,
                    outletId = outlet.outletId,
                    customerId = phone,
                    orderId = orderId,
                    paymentId = null,
                    type = "ORDER",
                  //  debitAmount = dueAmount,
                    debitAmount = totalCredit,
                    creditAmount = 0.0,
                    balanceAfter = newBalance,
                    note = "Credit sale Order #$srno",
                    createdAt = now,
                    deviceId = "POS"
                )

                ledgerDao.insert(ledgerEntry)
            }



            val paymentMode =
                if (payments.size > 1) "MIXED"
                else payments.firstOrNull()?.mode ?: "CREDIT"

            // =====================================================
            // ORDER MASTER
            // =====================================================

            val orderMaster = PosOrderMasterEntity(
                id = orderId,
                srno = srno,
                orderType = orderType,
                tableNo = tableName,
                customerName = deliveryAddress?.name ?: "Walk-in",
                customerPhone = deliveryAddress?.phone ?: "",
                customerId = resolvedCustomerId,
                dAddressLine1 = deliveryAddress?.line1,
                dAddressLine2 = deliveryAddress?.line2,
                dCity = deliveryAddress?.city,
                dState = deliveryAddress?.state,
                dZipcode = deliveryAddress?.zipcode,
                dLandmark = deliveryAddress?.landmark,

                itemTotal = itemSubtotal,
                taxTotal = taxTotal,
                discountTotal = discount,
                grandTotal = grandTotal,

                paymentMode = paymentMode,
                paymentStatus = paymentStatus,
//                paidAmount = totalPaid,
//                dueAmount = dueAmount,
                paidAmount = totalPaid,
                dueAmount = totalCredit,

                orderStatus = "COMPLETED",

                deviceId = "POS",
                deviceName = "POS",
                appVersion = "1.0",

                createdAt = now,
                updatedAt = now,

                syncStatus = "PENDING",
                lastSyncedAt = null,
                notes = null
            )


            val orderItems = kotItems
                .groupBy {
                    listOf(
                        it.productId,
                        it.basePrice,
                        it.taxRate,
                        it.note,
                        it.modifiersJson
                    )
                }
                .map { (_, group) ->

                    val first = group.first()
                    val quantity = group.sumOf { it.quantity }
                    val subtotal = first.basePrice * quantity

                    val taxPerItem =
                        if (first.taxType == "exclusive")
                            first.basePrice * (first.taxRate / 100)
                        else 0.0

                    val taxTotalItem = taxPerItem * quantity

                    PosOrderItemEntity(
                        id = UUID.randomUUID().toString(),
                        orderMasterId = orderId,
                        productId = first.productId,
                        name = first.name,
                        categoryId = first.categoryId,
                        parentId = first.parentId,
                        isVariant = first.isVariant,
                        basePrice = first.basePrice,
                        quantity = quantity,
                        itemSubtotal = subtotal,
                        taxRate = first.taxRate,
                        taxType = first.taxType,
                        taxAmountPerItem = taxPerItem,
                        taxTotal = taxTotalItem,
                        // ✅ ADD THESE (if not already in entity)
                        note = first.note,
                        modifiersJson = first.modifiersJson,
                        finalPricePerItem = first.basePrice + taxPerItem,
                        finalTotal = subtotal + taxTotalItem,
                        createdAt = now,


                    )
                }


            // Save order and items atomically


            val paymentEntities = payments.map {
                PosOrderPaymentEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    ownerId = outlet.ownerId,
                    outletId = outlet.outletId,
                    amount = it.amount,
                    mode = it.mode,
                    provider = null,
                    method = null,
                    status = "SUCCESS",
                    deviceId = "POS",
                    createdAt = now,
                    syncStatus = "PENDING"
                )
            }

            withContext(Dispatchers.IO) {

                // 1️⃣ Save Order
                orderMasterDao.insert(orderMaster)
                orderProductDao.insertAll(orderItems)

                // 2️⃣ Save Payments (ONLY if paid > 0)
                if (payments.isNotEmpty() && totalPaid > 0) {

                    val paymentEntities = payments.map {
                        PosOrderPaymentEntity(
                            id = UUID.randomUUID().toString(),
                            orderId = orderId,
                            ownerId = outlet.ownerId,
                            outletId = outlet.outletId,
                            amount = it.amount,
                            mode = it.mode,
                            provider = null,
                            method = null,
                            status = "SUCCESS",
                            deviceId = "POS",
                            createdAt = now,
                            syncStatus = "PENDING"
                        )
                    }

                    paymentRepository.insertPayments(paymentEntities)
                }

                // 3️⃣ Finalize table
                repository.finalizeTableAfterPayment(tableId)
            }


            // Print and finish
            printOrder(orderMaster, orderItems)

        }
    }


    fun deleteItem(itemId: String) {
        viewModelScope.launch {
            try {
                kotItemDao.deleteItemById(itemId)
                Log.d("DELETE", "Item deleted: $itemId")

                // Optional: refresh list to update UI
//                val newList = kotItemDao.getItemsForTableSync(currentTableId)
//                _uiState.update { it.copy(items = newList) }

            } catch (e: Exception) {
                Log.e("DELETE", "Failed to delete item", e)
            }
        }
    }
    // --------------------------------------------------------
    // Set Delivery Address
    // --------------------------------------------------------
    fun setDeliveryAddress(address: DeliveryAddressUiState) {
        _deliveryAddress.value = address
    }

    // --------------------------------------------------------
    // Printing (Unified print pipeline)
    // --------------------------------------------------------
    private suspend fun printOrder(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ) = withContext(Dispatchers.IO) {
        val printOrder = PrintOrderBuilder.build(order, items)

        val outlet = outletDao.getOutlet()
        val outletInfo = OutletMapper.fromEntity(outlet)

        printerManager.printTextNew(PrinterRole.BILLING, printOrder)
        Log.d("PRINT_ORDER", "Receipt printed successfully | orderNo=${order.srno}")
    }


    fun getDoneItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {
        return kotItemDao.getDoneItemsForTable(orderRef)
    }


    // file: BillViewModel.kt (inside the class)
    fun updateItemQuantity(itemId: String, newQty: Int) {

        viewModelScope.launch {

            val qty = newQty.coerceAtLeast(0)

            Log.d("EDIT_DEBUG", "Requested update itemId=$itemId newQty=$qty")

            val targetUi = _uiState.value.items
                .find { it.id == itemId }

            if (targetUi == null) {
                Log.d("EDIT_DEBUG", "❌ targetUi NOT FOUND")
                return@launch
            }

            Log.d("EDIT_DEBUG", "✅ targetUi found name=${targetUi.name} qty=${targetUi.quantity}")

            val allItems = kotItemDao.getItemsForTableSync(tableId)

            Log.d("EDIT_DEBUG", "DB items count=${allItems.size}")

            val groupedItems = allItems.filter {
                it.productId == targetUi.productId &&
                        it.basePrice == targetUi.basePrice &&
                        it.taxRate == targetUi.taxRate &&
                        (it.note ?: "") == targetUi.note &&
                        (it.modifiersJson ?: "") == targetUi.modifiersJson &&
                        it.status == "DONE"
            }

            Log.d("EDIT_DEBUG", "Grouped items found=${groupedItems.size}")
            groupedItems.forEach {
                Log.d("EDIT_DEBUG", "Match -> id=${it.id} qty=${it.quantity}")
            }

            // Delete
            groupedItems.forEach {
                kotItemDao.deleteItemById(it.id)
            }

            Log.d("EDIT_DEBUG", "Deleted grouped items")

            if (qty > 0 && groupedItems.isNotEmpty()) {

                val template = groupedItems.first()

                kotItemDao.insert(
                    template.copy(
                        id = UUID.randomUUID().toString(),
                        quantity = qty
                    )
                )

                Log.d("EDIT_DEBUG", "Inserted new row qty=$qty")
            }

            val after = kotItemDao.getItemsForTableSync(tableId)
            Log.d("EDIT_DEBUG", "After update DB total qty = ${after.sumOf { it.quantity }}")
        }
    }





}
