package com.it10x.foodappgstav7_03.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_03.data.pos.entities.PosCustomerEntity

@Dao
interface PosCustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: PosCustomerEntity)

    @Update
    suspend fun update(customer: PosCustomerEntity)

    @Query("SELECT * FROM pos_customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): PosCustomerEntity?

    @Query("UPDATE pos_customers SET currentDue = currentDue + :amount WHERE id = :customerId")
    suspend fun increaseDue(customerId: String, amount: Double)

    @Query("UPDATE pos_customers SET currentDue = currentDue - :amount WHERE id = :customerId")
    suspend fun decreaseDue(customerId: String, amount: Double)
}
