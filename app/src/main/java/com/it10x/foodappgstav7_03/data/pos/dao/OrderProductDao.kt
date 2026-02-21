package com.it10x.foodappgstav7_03.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_03.data.pos.entities.PosOrderItemEntity

import kotlinx.coroutines.flow.Flow



@Dao
interface OrderProductDao {

    // -------------------------
    // INSERT
    // -------------------------
    @Insert
    suspend fun insertAll(items: List<PosOrderItemEntity>)

    // -------------------------
    // ORDER DETAILS (single order)
    // -------------------------
    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    fun getByOrderId(orderId: String): Flow<List<PosOrderItemEntity>>

    @Query("SELECT * FROM pos_order_items WHERE orderMasterId = :orderId")
    suspend fun getByOrderIdSync(orderId: String): List<PosOrderItemEntity>

    // -------------------------
    // 🔥 FINAL BILL (ALL OPEN ORDERS OF A TABLE)
    // -------------------------
    @Query("""
        SELECT i.*
        FROM pos_order_items i
        INNER JOIN pos_order_master o
            ON i.orderMasterId = o.id
        WHERE o.tableNo = :tableNo
          AND o.orderStatus IN ('NEW', 'OPEN')
        ORDER BY o.createdAt ASC, i.createdAt ASC
    """)
    suspend fun getAllItemsForTable(tableNo: String): List<PosOrderItemEntity>


    @Query("""
    SELECT i.* FROM pos_order_items i
    INNER JOIN pos_order_master m
        ON i.orderMasterId = m.id
    WHERE m.tableNo = :tableNo
      AND m.orderStatus IN ('NEW', 'OPEN')
""")
    suspend fun getItemsForTable(tableNo: String): List<PosOrderItemEntity>

    @Query("""
SELECT categoryId, SUM(finalTotal) as total
FROM pos_order_items
GROUP BY categoryId
""")
    suspend fun getCategorySalesRaw(): List<CategorySaleRaw>


    @Query("""
    SELECT c.name AS categoryName, SUM(op.finalTotal) AS total
    FROM pos_order_items op
    INNER JOIN categories c ON op.categoryId = c.id
    INNER JOIN pos_order_master om ON op.orderMasterId = om.id
    WHERE om.paymentStatus = 'PAID'
    AND om.createdAt BETWEEN :from AND :to
    GROUP BY c.name
""")
    suspend fun getCategorySalesBetween(
        from: Long,
        to: Long
    ): List<CategorySalesResult>






}


//data class CategorySaleRaw(
//    val categoryId: String,
//    val total: Double
//)

data class CategorySalesResult(
    val categoryName: String,
    val total: Double
)