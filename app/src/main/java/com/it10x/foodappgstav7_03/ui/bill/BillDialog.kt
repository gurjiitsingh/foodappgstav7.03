package com.it10x.foodappgstav7_03.ui.bill

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import com.it10x.foodappgstav7_03.ui.components.NumPad

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

    val context = LocalContext.current
    //--------------- PHONE ---------------
    val customerPhone = remember { mutableStateOf("") }
    var activeInput by remember { mutableStateOf<String?>(null) }
    val discountFlat = remember { mutableStateOf("") }
    val discountPercent = remember { mutableStateOf("") }
    val creditAmount = remember { mutableStateOf("") }
    var showRemainingOptions by remember { mutableStateOf(false) }
    var showDiscount by remember { mutableStateOf(false) }
    var partialPaidAmount by remember { mutableStateOf(0.0) } // track paid amount so far

    val usedPaymentModes = remember { mutableStateListOf<String>() }
    var isCreditSelected by remember { mutableStateOf(false) }




    val paymentList = remember { mutableStateListOf<PaymentInput>() }   // ✅ ADD THIS LINE


    val billViewModel: BillViewModel = viewModel(
        key = "BillVM_${sessionId}",
        factory = BillViewModelFactory(
            application = LocalContext.current.applicationContext as Application,
            tableId = tableId ?: orderType,
            tableName = selectedTableName,
            orderType = orderType
        )
    )
    val totalAmount = billViewModel.uiState.value.total
    val remainingAmount = (totalAmount - partialPaidAmount).coerceAtLeast(0.0)
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(1f)
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
                                phone = customerPhone.value
                            )

                            onDismiss()
                        }
                    )


                }

                // ========= RIGHT COLUMN (Discount + Payment Buttons) =========
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 6.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

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
                            .clickable { activeInput = "PHONE" }
                    ) {
                        OutlinedTextField(
                            value = customerPhone.value,
                            onValueChange = {},
                            label = { Text("Customer Phone") },
                            readOnly = true,
                            enabled = false,
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor =
                                    if (activeInput == "PHONE") Color(0xFF2E3B2F)
                                    else Color(0xFF2A2A2A),

                                disabledBorderColor =
                                    if (activeInput == "PHONE") Color(0xFF4CAF50)
                                    else Color.Gray,

                                disabledTextColor = Color.White,
                                disabledLabelColor = Color.LightGray
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 15.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)   // 👈 keeps safe height
                        )
                    }




                    TextButton(
                        onClick = { showDiscount = !showDiscount }
                    ) {
                        Text(if (showDiscount) "Hide Discount" else "Add Discount")
                    }




                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
//                        Text("Discount", style = MaterialTheme.typography.titleSmall)
                        // -------- FLAT --------
                        if (showDiscount) {


                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "FLAT"
                                        discountPercent.value = ""
                                        billViewModel.setPercentDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountFlat.value,
                                    onValueChange = {},
                                    label = { Text("Flat") },
                                    readOnly = true,
                                    enabled = false,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (activeInput == "FLAT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        unfocusedBorderColor = if (activeInput == "FLAT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        disabledBorderColor = if (activeInput == "FLAT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        disabledTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // -------- PERCENT --------
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        activeInput = "PERCENT"
                                        discountFlat.value = ""
                                        billViewModel.setFlatDiscount(0.0)
                                    }
                            ) {
                                OutlinedTextField(
                                    value = discountPercent.value,
                                    onValueChange = {},
                                    label = { Text("%") },
                                    readOnly = true,
                                    enabled = false, colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (activeInput == "PERCENT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        unfocusedBorderColor = if (activeInput == "PERCENT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        disabledBorderColor = if (activeInput == "PERCENT") Color(
                                            0xFF4CAF50
                                        ) else Color.Gray,
                                        disabledTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            TextButton(
                                onClick = {
                                    discountFlat.value = ""
                                    discountPercent.value = ""
                                    billViewModel.setFlatDiscount(0.0)
                                    billViewModel.setPercentDiscount(0.0)
                                    activeInput = null
                                }
                            ) {
                                Text("❌")
                            }
                        }

                    }









                    Spacer(modifier = Modifier.height(4.dp))




                    // ---------- CREDIT OR PAYLATER ----------










// Track used payment methods to prevent duplicates



                    Spacer(Modifier.height(6.dp))
                    Text("Select Options", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isCreditSelected) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()

                            ) {

                                // ---------- CREDIT FIELD ----------
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeInput = "CREDIT"
                                        }
                                ) {
                                    OutlinedTextField(
                                        value = creditAmount.value,
                                        onValueChange = {},
                                        label = { Text("Credit Amount") },
                                        readOnly = true,
                                        enabled = false,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }



                                Spacer(modifier = Modifier.height(6.dp))

                                // ---------- CREDIT NUMPAD ----------


                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {

                                    // ---------- CANCEL ----------
                                    OutlinedButton(
                                        onClick = {
                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                    ) {
                                        Text("Cancel")
                                    }

                                    // ---------- CONFIRM ----------
                                    Button(
                                        onClick = {

                                            if (customerPhone.value.isBlank()) {
                                                Toast.makeText(
                                                    context,
                                                    "Phone number required for credit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@Button
                                            }

                                            val amount = creditAmount.value.toDoubleOrNull() ?: return@Button

                                            if (amount <= 0.0) return@Button
                                            if (amount > remainingAmount) return@Button

                                            paymentList.add(PaymentInput("CREDIT", amount))
                                            partialPaidAmount += amount

                                            creditAmount.value = ""
                                            activeInput = null
                                            isCreditSelected = false

                                            if (partialPaidAmount >= totalAmount) {
                                                billViewModel.payBill(
                                                    payments = paymentList.toList(),
                                                    name = "Customer",
                                                    phone = customerPhone.value
                                                )
                                                onDismiss()
                                            } else {
                                                showRemainingOptions = true
                                            }
                                        },

                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFC107),
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Text("Confirm")
                                    }
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


                                val amount = creditAmount.value.toDoubleOrNull() ?: 0.0
                                if (customerPhone.value.isBlank()) {
                                    Toast.makeText(
                                        context,
                                        "Phone number required for delivery",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }

                                if (remainingAmount > 0) {
                                    billViewModel.payBill(
                                        payments = listOf(
                                            PaymentInput("DELIVERY_PENDING", remainingAmount)
                                        ),
                                        name = "Customer",
                                        phone = customerPhone.value
                                    )

                                    usedPaymentModes.add("PENDING")
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
//                    Text("Select Payment", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))

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

                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("CASH", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = customerPhone.value
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

                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("CARD", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = customerPhone.value
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


                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("UPI", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = customerPhone.value
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
                                    val finalPayments = paymentList.toMutableList()
                                    finalPayments.add(PaymentInput("WALLET", amountToPay))

                                    billViewModel.payBill(
                                        payments = finalPayments,
                                        name = "Customer",
                                        phone = customerPhone.value
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


// ===============================
// GLOBAL NUMPAD (Single Keyboard)
// ===============================



                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))

                        NumPad { label ->
                            handleInput(
                                label = label,
                                activeInput = activeInput,
                                customerPhone = customerPhone,
                                discountFlat = discountFlat,
                                discountPercent = discountPercent,
                                creditAmount = creditAmount,
                                billViewModel = billViewModel
                            )
                        }











                }

            }
        }
    }
}



fun handleInput(
    label: String,
    activeInput: String?,
    customerPhone: MutableState<String>,
    discountFlat: MutableState<String>,
    discountPercent: MutableState<String>,
    creditAmount: MutableState<String>,
    billViewModel: BillViewModel
) {
    when (activeInput) {

        "PHONE" -> {
            when (label) {
                "←" -> if (customerPhone.value.isNotEmpty())
                    customerPhone.value = customerPhone.value.dropLast(1)

                "." -> {}

                else -> if (customerPhone.value.length < 10)
                    customerPhone.value += label
            }
        }

        "FLAT" -> {
            when (label) {
                "←" -> discountFlat.value = discountFlat.value.dropLast(1)
                else -> discountFlat.value += label
            }

            billViewModel.setFlatDiscount(
                discountFlat.value.toDoubleOrNull() ?: 0.0
            )
        }

        "PERCENT" -> {
            when (label) {
                "←" -> discountPercent.value = discountPercent.value.dropLast(1)
                else -> discountPercent.value += label
            }

            billViewModel.setPercentDiscount(
                discountPercent.value.toDoubleOrNull() ?: 0.0
            )
        }

        "CREDIT" -> {
            when (label) {
                "←" -> creditAmount.value = creditAmount.value.dropLast(1)
                else -> creditAmount.value += label
            }
        }
    }
}
