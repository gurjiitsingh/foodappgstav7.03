package com.it10x.foodappgstav7_03.data.pos.repository

import com.it10x.foodappgstav7_03.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_03.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerLedgerEntity
import java.util.*

class CustomerLedgerRepository(
    private val customerDao: PosCustomerDao,
    private val ledgerDao: PosCustomerLedgerDao
) {

    suspend fun addOrderDebit(
        customerId: String,
        ownerId: String,
        outletId: String,
        orderId: String,
        amount: Double
    ) {

        val lastBalance = ledgerDao.getLastBalance(customerId) ?: 0.0
        val newBalance = lastBalance + amount

        ledgerDao.insert(
            PosCustomerLedgerEntity(
                id = UUID.randomUUID().toString(),
                ownerId = ownerId,
                outletId = outletId,
                customerId = customerId,
                orderId = orderId,
                paymentId = null,
                type = "ORDER",
                debitAmount = amount,
                creditAmount = 0.0,
                balanceAfter = newBalance,
                note = "Order Credit",
                createdAt = System.currentTimeMillis(),
                deviceId = "POS"
            )
        )

        customerDao.increaseDue(customerId, amount)
    }


    suspend fun addPaymentCredit(
        customerId: String,
        ownerId: String,
        outletId: String,
        paymentId: String,
        amount: Double
    ) {

        val lastBalance = ledgerDao.getLastBalance(customerId) ?: 0.0
        val newBalance = (lastBalance - amount).coerceAtLeast(0.0)

        ledgerDao.insert(
            PosCustomerLedgerEntity(
                id = UUID.randomUUID().toString(),
                ownerId = ownerId,
                outletId = outletId,
                customerId = customerId,
                orderId = null,
                paymentId = paymentId,
                type = "PAYMENT",
                debitAmount = 0.0,
                creditAmount = amount,
                balanceAfter = newBalance,
                note = "Settlement Payment",
                createdAt = System.currentTimeMillis(),
                deviceId = "POS"
            )
        )

        customerDao.decreaseDue(customerId, amount)
    }
}
