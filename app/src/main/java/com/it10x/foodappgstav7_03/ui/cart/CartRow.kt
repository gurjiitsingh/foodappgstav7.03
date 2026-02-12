package com.it10x.foodappgstav7_03.ui.cart

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.it10x.foodappgstav7_03.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_03.ui.theme.PosError
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*

@Composable
fun CartRow(
    item: PosCartEntity,
    tableNo: String,
    cartViewModel: CartViewModel,
    onCartActionDirectMoveToBill: (item: PosCartEntity, print: Boolean) -> Unit
) {

    var showNoteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🗑️ Delete button
        IconButton(
            onClick = { cartViewModel.removeFromCart(item.productId, tableNo) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Item",
                tint = Color(0xFFDC2626)
            )
        }

        IconButton(
            onClick = { showNoteDialog = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Add Note",
                tint = Color(0xFFFFB703)
            )
        }


        // 🧾 Item name + price
        // 🧾 Item name + note + price
        Row(
            modifier = Modifier
                .weight(2.0f)
                .padding(start = 4.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = item.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = Color(0xFFE0E0E0)
                )

                item.note?.let { note ->
                    if (note.isNotBlank()) {
                        Text(
                            text = "📝 $note",
                            fontSize = 12.sp,
                            color = Color(0xFFFFB703)
                        )
                    }
                }
            }

            Text(
                text = "₹${item.basePrice}",
                fontSize = 13.sp,
                color = Color(0xFFBDBDBD)
            )
        }


        // ➕ Quantity controls (more readable)
        Row(
            modifier = Modifier
                .weight(0.55f)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = { cartViewModel.decrease(item.productId, tableNo) },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFDC2626), shape = MaterialTheme.shapes.small)
            ) {
                Text("−", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Text(
                text = item.quantity.toString(), // 🔹 no leading zero
                modifier = Modifier.padding(horizontal = 14.dp), // 🔹 added more spacing between − & +
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color(0xFFEEEEEE) // light gray for qty
            )

            IconButton(
                onClick = { cartViewModel.increase(item) },
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF16A34A), shape = MaterialTheme.shapes.small)
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }

        // 🍳 Bill / Kitchen buttons
        Row(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onCartActionDirectMoveToBill(item, true) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SoupKitchen,
                    contentDescription = "Kitchen",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Bill",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

            OutlinedButton(
                onClick = { onCartActionDirectMoveToBill(item, false) },
                border = BorderStroke(1.dp, PosError),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "Bill Only",
                    tint = PosError,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }

    Divider(color = Color.LightGray.copy(alpha = 0.25f))

    if (showNoteDialog) {

        var noteText by remember { mutableStateOf(item.note ?: "") }

        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Kitchen Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Special Instructions") },
                    singleLine = false
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        cartViewModel.updateNote(item, noteText)
                        showNoteDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNoteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}
