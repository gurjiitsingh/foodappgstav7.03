package com.it10x.foodappgstav7_03.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CustomerLedgerScreen(
    viewModel: CustomerLedgerViewModel
) {

    val ledger by viewModel.ledger.collectAsState()

    Button(
        onClick = {
//            repository.addPaymentCredit(
//                customerId = customerId,
//                ownerId = "...",
//                outletId = "...",
//                paymentId = UUID.randomUUID().toString(),
//                amount = amount
//            )
//            viewModel.loadLedger()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text("Collect Payment")
    }

    Scaffold(
        topBar = {
           // TopAppBar(title = { Text("Customer Statement") })
        }
    ) {

        padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(ledger) { entry ->
                LedgerRow(entry)
            }
        }
    }
}

@Composable
fun LedgerRow(entry: com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerLedgerEntity) {

    val formatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {

            Text("Type: ${entry.type}")
            Text("Debit: ₹${entry.debitAmount}")
            Text("Credit: ₹${entry.creditAmount}")
            Text("Balance: ₹${entry.balanceAfter}")
            Text("Date: ${formatter.format(Date(entry.createdAt))}")
        }
    }
}
