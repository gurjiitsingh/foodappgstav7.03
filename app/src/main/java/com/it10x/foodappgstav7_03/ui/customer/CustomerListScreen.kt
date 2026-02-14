package com.it10x.foodappgstav7_03.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerEntity

@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onCustomerClick: (String) -> Unit
) {

    val customers = viewModel.customers

    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { query ->
                    searchQuery = query
                    viewModel.search(query)
                },
                label = { Text("Search by name or phone") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                singleLine = true
            )

            LazyColumn {
                items(customers) { customer ->
                    CustomerRow(customer, onCustomerClick)
                }
            }
        }
    }
}


@Composable
fun CustomerRow(
    customer: PosCustomerEntity,
    onCustomerClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCustomerClick(customer.id) }
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(customer.name ?: "No Name")
            Text("Phone: ${customer.phone}")
            Text("Due: ₹${customer.currentDue}")
        }
    }
}
