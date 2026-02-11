package com.it10x.foodappgstav7_03.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PosSearchKeyboardRight(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {

        val letterRows = listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L"),
            listOf("Z","X","C","V","B","N","M")
        )

        val numberColumns = listOf(
            listOf("1","2","3"),
            listOf("4","5","6"),
            listOf("7","8","9","0")
        )

        for (i in 0..2) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // 🔹 LEFT SIDE
                Row(
                    modifier = Modifier.weight(4f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    letterRows[i].forEach { key ->
                        KeyButtonStyled(
                            label = key,
                            weight = 1f,
                            height = 42.dp
                        ) { onKeyPress(key) }
                    }

                    if (i == 2) {
                        // Add CLEAR, OK and BACKSPACE in same row
                        KeyButtonStyled(
                            label = "CLEAR",
                            weight = 1f,
                            height = 42.dp
                        ) { onClear() }

                        KeyButtonStyled(
                            label = "OK",
                            weight = 1f,
                            height = 42.dp
                        ) { onClose() }

                        KeyButtonStyled(
                            label = "⌫",
                            weight = 1f,
                            height = 42.dp
                        ) { onBackspace() }
                    }
                }

                // 🔹 RIGHT SIDE NUMBERS
                Row(
                    modifier = Modifier.weight(1.4f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    numberColumns[i].forEach { key ->
                        KeyButtonStyled(
                            label = key,
                            weight = 1f,
                            height = 42.dp
                        ) { onKeyPress(key) }
                    }
                }
            }
        }
    }
}




@Composable
fun KeyButtonStyled(
    label: String,
    weight: Float = 1f,
    height: Dp = 44.dp,
    onClick: () -> Unit
) {
    val color = when (label) {
        "OK" -> Color(0xFF81C784)     // soft green
        "CLEAR" -> Color(0xFFFFCDD2)  // soft red
        "⌫" -> Color(0xFFBBDEFB)      // soft blue
        "SPACE" -> Color(0xFFFFF9C4)  // soft yellow
        else -> Color(0xFFE0E0E0)     // default gray
    }

    val textColor = when (label) {
        "OK" -> Color(0xFF1B5E20)
        "CLEAR" -> Color(0xFFB71C1C)
        "⌫" -> Color(0xFF0D47A1)
        "SPACE" -> Color(0xFF5D4037)
        else -> Color.Black
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            //.weight(weight)
            .height(height),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(vertical = 6.dp),
        shape = MaterialTheme.shapes.small,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(label, color = textColor)
    }
}
