package com.it10x.foodappgstav7_03.ui.payment

data class PaymentInput(
    val mode: String,       // CASH | UPI | CARD | CREDIT
    val amount: Double
)


