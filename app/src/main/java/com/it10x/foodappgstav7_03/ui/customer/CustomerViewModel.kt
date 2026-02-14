package com.it10x.foodappgstav7_03.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_03.data.pos.repository.CustomerRepository

class CustomerViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    var customers by mutableStateOf<List<PosCustomerEntity>>(emptyList())
        private set

    fun search(query: String) {
        viewModelScope.launch {
            customers = repository.search(query)
        }
    }

    fun loadAll() {
        viewModelScope.launch {
            customers = repository.search("")
        }
    }
}
