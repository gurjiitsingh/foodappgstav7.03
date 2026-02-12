package com.it10x.foodappgstav7_03.ui.pos

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_03.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_03.data.pos.entities.ProductEntity
import com.it10x.foodappgstav7_03.ui.cart.CartViewModel
import com.it10x.foodappgstav7_03.viewmodel.PosTableViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductList(
    filteredProducts: List<ProductEntity>,
    variants: List<ProductEntity>,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    posSessionViewModel: PosSessionViewModel
) {
    val sessionId by posSessionViewModel.sessionId.collectAsState()

    val sortedProducts = remember(filteredProducts) {
        filteredProducts.sortedBy { it.sortOrder }
    }

    // ✅ Rectangular grid with no vertical gaps
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        items(sortedProducts.size) { index ->
            val product = sortedProducts[index]

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
                color = MaterialTheme.colorScheme.surface,
                shape = RectangleShape
            ) {
                ParentProductCard(
                    product = product,
                    cartViewModel = cartViewModel,
                    tableViewModel = tableViewModel,
                    tableNo = tableNo,
                    sessionId = sessionId
                )
            }
        }
    }
}

@Composable
private fun ParentProductCard(
    product: ProductEntity,
    cartViewModel: CartViewModel,
    tableViewModel: PosTableViewModel,
    tableNo: String,
    sessionId: String
) {
    val cartItems by cartViewModel.cart.collectAsState()
    val currentQty = remember(cartItems) {
        cartItems
            .filter { it.tableId == tableNo && it.productId == product.id }
            .sumOf { it.quantity }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline)),
        color = MaterialTheme.colorScheme.surface,
        shape = RectangleShape
    ) {
        Column(
            modifier = Modifier
                .padding(11.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val price = when {
                product.discountPrice == null || product.discountPrice == 0.0 -> product.price
                else -> product.discountPrice
            }

            // ⭐ Product Name
            Text(
                text = toTitleCase(product.name),
                minLines = 2,
                maxLines = 2,
                lineHeight = 18.sp
            )

            // ⭐ Product Price and Code (conditionally visible)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "₹${price}",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )

                // ✅ Show code only if it's a valid text/number
                val code = product.searchCode?.trim()
                val isValidCode = !code.isNullOrEmpty() &&
                        code.lowercase() != "null" &&
                        code != "0" &&
                        code != "N/A" &&
                        code != "-"

                if (isValidCode) {
                    Text(
                        text = code!!,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ➕➖ Quantity Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ➖ Rectangular
                IconButton(
                    onClick = { cartViewModel.decrease(product.id, tableNo) },
                    modifier = Modifier
                        .size(width = 40.dp, height = 32.dp)
                        .background(MaterialTheme.colorScheme.surface, RectangleShape)
                        .border(1.5.dp, Color(0xFFD32F2F), RectangleShape)
                ) {
                    Text(
                        text = "−",
                        color = Color(0xFFD32F2F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ✅ Only show quantity if greater than 0
                if (currentQty > 0) {
                    Text(
                        text = currentQty.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ➕ Rectangular
                IconButton(
                    onClick = {
                        Log.d("CART_DEBUG", "Added item ${product.name}")

                        cartViewModel.addToCart(
                            PosCartEntity(
                                productId = product.id,
                                name = toTitleCase(product.name),
                                basePrice = price,
                                quantity = 1,
                                taxRate = product.taxRate ?: 0.0,
                                taxType = product.taxType ?: "inclusive",
                                parentId = null,
                                isVariant = false,
                                categoryId = product.categoryId,
                                sessionId = sessionId,
                                tableId = tableNo
                            )
                        )
                        tableNo.let { tableViewModel.markOrdering(it) }
                    },
                    modifier = Modifier
                        .size(width = 40.dp, height = 32.dp)
                        .background(Color(0xFFD32F2F), RectangleShape)
                ) {
                    Text("+", color = Color.White, fontSize = 18.sp)
                }
            }
        }
    }
}
