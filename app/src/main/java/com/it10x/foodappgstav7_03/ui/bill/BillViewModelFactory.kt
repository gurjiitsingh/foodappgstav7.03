package com.it10x.foodappgstav7_03.ui.bill

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_03.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_03.data.pos.repository.OrderSequenceRepository
import com.it10x.foodappgstav7_03.data.pos.repository.OutletRepository
import com.it10x.foodappgstav7_03.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_03.data.pos.repository.POSPaymentRepository
import com.it10x.foodappgstav7_03.printer.PrinterManager

class BillViewModelFactory(
    private val application: Application,
    private val tableId: String,
    private val tableName: String,
    private val orderType: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {

            val db = AppDatabaseProvider.get(application)

            val orderSequenceRepository = OrderSequenceRepository(db)

            val printerManager = PrinterManager(application.applicationContext)

            val ordersRepository = POSOrdersRepository(
                db = db,
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                cartDao = db.cartDao(),
                tableDao = db.tableDao()
            )

            // ✅ FIXED HERE
            val paymentRepository = POSPaymentRepository(
                paymentDao = db.posOrderPaymentDao()
            )

            @Suppress("UNCHECKED_CAST")
            return BillViewModel(
                kotItemDao = db.kotItemDao(),
                orderMasterDao = db.orderMasterDao(),
                orderProductDao = db.orderProductDao(),
                orderSequenceRepository = orderSequenceRepository,
                outletDao = db.outletDao(),
                tableId = tableId,
                tableName = tableName,
                orderType = orderType,
                repository = ordersRepository,
                printerManager = printerManager,
                outletRepository = OutletRepository(db.outletDao()),
                paymentRepository = paymentRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

