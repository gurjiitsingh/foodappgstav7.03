package com.it10x.foodappgstav7_03.ui.bill

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.it10x.foodappgstav7_03.ui.bill.BillViewModel
import com.it10x.foodappgstav7_03.ui.bill.BillViewModelFactory
import com.it10x.foodappgstav7_03.ui.payment.PaymentInput

@Composable
fun BillDialog(
    showBill: Boolean,
    onDismiss: () -> Unit,
    sessionId: String?,
    tableId: String?,
    orderType: String,
    selectedTableName: String
) {
    if (!showBill || sessionId == null) return
    //--------------- PHONE ---------------
    var customerPhone by remember { mutableStateOf("") }
    var showPhonePad by remember { mutableStateOf(false) }

    val billViewModel: BillViewModel = viewModel(
        key = "BillVM_${sessionId}",
        factory = BillViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            tableId = tableId ?: orderType,
            tableName = selectedTableName,
            orderType = orderType
        )
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ========= LEFT COLUMN (Bill List + Totals) =========
                Column(
                    modifier = Modifier
                        .weight(2.2f)
                        .padding(8.dp)
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState())
                ) {


                    Text(
                        "Final Bill",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.4f))
                    BillScreen(
                        viewModel = billViewModel,
                        onPayClick = { paymentType ->

                            val totalAmount = billViewModel.uiState.value.total

                            billViewModel.payBill(
                                payments = listOf(
                                    PaymentInput(
                                        mode = paymentType.name,
                                        amount = totalAmount
                                    )
                                ),
                                name = "Customer",
                                phone = customerPhone
                            )

                            onDismiss()
                        }
                    )


                }

                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {



                    val discountFlat = remember { mutableStateOf("") }
                    val discountPercent = remember { mutableStateOf("") }
                    var activeField by remember { mutableStateOf("FLAT") }
                    var showRemainingOptions by remember { mutableStateOf(false) }






                    // ---------------- DISCOUNT SECTION ----------------

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Actions",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )

                        // ✅ Compact Close button (top-right)
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .height(28.dp)
                                .width(70.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB71C1C), // POS red
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text("Close", fontSize = 12.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPhonePad = true }
                    ) {
                        OutlinedTextField(
                            value = customerPhone,
                            onValueChange = {},
                            label = { Text("Customer Phone (Required for Credit)") },
                            singleLine = true,
                            readOnly = true,
                            enabled = false,   // very important
                            modifier = Modifier.fillMaxWidth()
                        )
                    }


                    if (showPhonePad) {

                        Spacer(modifier = Modifier.height(6.dp))

                        NumPad { label ->
                            when (label) {
                                "←" -> if (customerPhone.isNotEmpty())
                                    customerPhone = customerPhone.dropLast(1)

                                "." -> {} // ignore dot for phone

                                else -> {
                                    if (customerPhone.length < 10) {  // optional limit
                                        customerPhone += label
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Button(
                            onClick = { showPhonePad = false }, // ✅ hide keyboard
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            )
                        ) {
                            Text("OK")
                        }
                    }


                    Text("Discount", style = MaterialTheme.typography.titleSmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // FLAT BOX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (activeField == "FLAT") Color(0xFF424242) else Color(
                                        0xFF616161
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (activeField == "FLAT") Color(0xFF90CAF9) else Color(
                                        0xFFBDBDBD
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeField = "FLAT"
                                    discountPercent.value = "" // ✅ clear other field
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = discountFlat.value.ifEmpty { "Flat" },
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // PERCENT BOX
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (activeField == "PERCENT") Color(0xFF424242) else Color(
                                        0xFF616161
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.dp,
                                    if (activeField == "PERCENT") Color(0xFF90CAF9) else Color(
                                        0xFFBDBDBD
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    activeField = "PERCENT"
                                    discountFlat.value = "" // ✅ clear other field
                                }
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = discountPercent.value.ifEmpty { "%" },
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }


                    // ---------- CUSTOM NUM PAD ----------
                    val buttons = listOf(
                        listOf("1", "2", "3", "4", "5", "6"),
                        listOf("7", "8", "9", "0", ".", "←")
                    )

                    buttons.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            row.forEach { label ->
                                Button(
                                    onClick = {
                                        when (label) {
                                            "←" -> {
                                                if (activeField == "FLAT" && discountFlat.value.isNotEmpty()) {
                                                    discountFlat.value =
                                                        discountFlat.value.dropLast(1)
                                                    billViewModel.setFlatDiscount(
                                                        discountFlat.value.toDoubleOrNull() ?: 0.0
                                                    )
                                                } else if (activeField == "PERCENT" && discountPercent.value.isNotEmpty()) {
                                                    discountPercent.value =
                                                        discountPercent.value.dropLast(1)
                                                    billViewModel.setPercentDiscount(
                                                        discountPercent.value.toDoubleOrNull()
                                                            ?: 0.0
                                                    )
                                                }
                                            }

                                            else -> {
                                                if (activeField == "FLAT") {
                                                    discountFlat.value += label
                                                    billViewModel.setFlatDiscount(
                                                        discountFlat.value.toDoubleOrNull() ?: 0.0
                                                    )
                                                    discountPercent.value = ""
                                                } else {
                                                    discountPercent.value += label
                                                    billViewModel.setPercentDiscount(
                                                        discountPercent.value.toDoubleOrNull()
                                                            ?: 0.0
                                                    )
                                                    discountFlat.value = ""
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    contentPadding = PaddingValues(vertical = 2.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE0E0E0), // soft gray
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(label, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Divider(Modifier.padding(vertical = 4.dp))


                    // ---------- CREDIT OR PAYLATER ----------

                    var creditAmount by remember { mutableStateOf("") } // store as String
                    var partialPaidAmount by remember { mutableStateOf(0.0) } // track paid amount so far
                    val totalAmount = billViewModel.uiState.value.total
                    val remainingAmount = (totalAmount - partialPaidAmount).coerceAtLeast(0.0)
                    var isCreditSelected by remember { mutableStateOf(false) }




                    val paymentList = remember { mutableStateListOf<PaymentInput>() }   // ✅ ADD THIS LINE







// Track used payment methods to prevent duplicates
                    val usedPaymentModes = remember { mutableStateListOf<String>() }


                    Spacer(Modifier.height(6.dp))
                    Text("Other Options", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCreditSelected) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                OutlinedTextField(
                                    value = if (creditAmount.isEmpty()) "0" else creditAmount,
                                    onValueChange = {}, // input via NumPad only
                                    label = { Text("Credit Amount") },
                                    singleLine = true,
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Custom NumPad
                                NumPad { label ->
                                    when (label) {
                                        "←" -> if (creditAmount.isNotEmpty()) creditAmount =
                                            creditAmount.dropLast(1)

                                        "." -> if (!creditAmount.contains(".")) creditAmount += label
                                        else -> creditAmount += label
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        val amount = creditAmount.toDoubleOrNull() ?: 0.0
                                        if (customerPhone.isBlank()) {
                                            Log.e("CREDIT", "Phone required")
                                            return@Button
                                        }
                                        if (amount <= 0.0) return@Button
                                        if (amount > remainingAmount) return@Button

                                        // ✅ Add to local payment list ONLY
                                        paymentList.add(PaymentInput("CREDIT", amount))
                                        partialPaidAmount += amount

                                        creditAmount = ""

                                        if (partialPaidAmount >= totalAmount) {
                                            // Now submit
                                            billViewModel.payBill(
                                                payments = paymentList.toList(),
                                                name = "Customer",
                                                phone = customerPhone
                                            )
                                            onDismiss()
                                        } else {
                                            showRemainingOptions = true
                                            isCreditSelected = false
                                        }
                                    }
                                    ,
                                    modifier = Modifier.fillMaxWidth().height(38.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black)
                                ) {
                                    Text("Confirm Credit", fontSize = 13.sp)
                                }

                            }
                        }

                        // ---------- Buttons ----------
                        // Credit Button
                        Button(
                            onClick = { isCreditSelected = true },
                         //   enabled = customerPhone.isNotBlank() && creditAmount.isNotBlank(),
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107), contentColor = Color.Black)
                        ) { Text("💳 Credit", fontSize = 13.sp) }

                        // Pay Later Button
                        Button(
                            onClick = {
                                if (remainingAmount > 0) {
                                    billViewModel.payBill(
                                        payments = listOf(PaymentInput("PAY_LATER", remainingAmount)),
                                        name = "Customer",
                                        phone = customerPhone
                                    )

                                    usedPaymentModes.add("PAY_LATER")
                                }
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9E9E9E), contentColor = Color.White)
                        ) { Text("🕒 Pay Later", fontSize = 13.sp) }
                    }


                    if (showRemainingOptions && remainingAmount > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text("Pay Remaining: ₹$remainingAmount", style = MaterialTheme.typography.titleSmall)
                        Text("Using following methods")

                    }

                    // ---------- PAYMENT BUTTONS (Compact, Pastel Colors) ----------
                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(6.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 💵 CASH + 💳 CARD
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total

                                    billViewModel.payBill(
                                        payments = listOf(PaymentInput("CASH", amountToPay)),
                                        name = "Customer",
                                        phone = customerPhone
                                    )


                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50),  // green
                                    contentColor = Color.White
                                )
                            ) { Text("💵 Cash", fontSize = 13.sp) }

                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total

                                    billViewModel.payBill(
                                        payments = listOf(PaymentInput("CARD", amountToPay)),
                                        name = "Customer",
                                        phone = customerPhone
                                    )
                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1976D2),  // blue
                                    contentColor = Color.White
                                )
                            ) { Text("💳 Card", fontSize = 13.sp) }
                        }

// 📱 UPI + 💰 WALLET
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total

                                    billViewModel.payBill(
                                        payments = listOf(PaymentInput("UPI", amountToPay)),
                                        name = "Customer",
                                        phone = customerPhone
                                    )
                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800),  // orange
                                    contentColor = Color.White
                                )
                            ) { Text("📱 UPI", fontSize = 13.sp) }

                            Button(
                                onClick = {
                                    val amountToPay = if (showRemainingOptions) remainingAmount else billViewModel.uiState.value.total
                                 billViewModel.payBill(
                                        payments = listOf(PaymentInput("WALLET", amountToPay)),
                                        name = "Customer",
                                        phone = customerPhone
                                    )

                                    partialPaidAmount += amountToPay
                                    onDismiss()
                                },
                                modifier = Modifier.weight(1f).height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0),  // purple
                                    contentColor = Color.White
                                )
                            ) { Text("💰 Wallet", fontSize = 13.sp) }
                        }

                    }










                }

            }
        }
    }
}


@Composable
fun NumPad(
    onInput: (String) -> Unit
) {
    val buttons = listOf(
        listOf("1", "2", "3", "4", "5", "6"),
        listOf("7", "8", "9", "0", ".", "←")
    )

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        buttons.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = { onInput(label) },
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp),
                        contentPadding = PaddingValues(vertical = 2.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
