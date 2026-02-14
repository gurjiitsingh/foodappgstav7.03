package com.it10x.foodappgstav7_03.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerLedgerEntity
import com.it10x.foodappgstav7_03.data.pos.repository.CustomerLedgerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerLedgerViewModel(
    private val repository: CustomerLedgerRepository,
    private val customerId: String
) : ViewModel() {

    private val _ledger = MutableStateFlow<List<PosCustomerLedgerEntity>>(emptyList())
    val ledger: StateFlow<List<PosCustomerLedgerEntity>> = _ledger

    init {
        loadLedger()
    }

    fun loadLedger() {
        viewModelScope.launch {
            _ledger.value = repository.getLedger(customerId)
        }
    }
}
