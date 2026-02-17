package com.it10x.foodappgstav7_03.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* =========================
   🔧 KEYBOARD SIZE PRESETS
   Comment what you don't want
   ========================= */

// --- Key height presets ---
val KEY_HEIGHT_56 = 56.dp
val KEY_HEIGHT_64 = 64.dp   // ✅ recommended default
val KEY_HEIGHT_72 = 72.dp

// --- Font size presets ---
val FONT_18 = 18.sp
val FONT_22 = 22.sp   // ✅ recommended default
val FONT_26 = 26.sp

// --- Spacing presets ---
val SPACE_6 = 6.dp
val SPACE_8 = 8.dp   // ✅ recommended default
val SPACE_10 = 10.dp

// --- Space key width presets ---
val SPACE_WEIGHT_SMALL = 1.5f
val SPACE_WEIGHT_MEDIUM = 2f   // ✅ recommended default
val SPACE_WEIGHT_BIG = 2.5f


@Composable
fun PosTouchKeyboard(
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    onMore: () -> Unit
) {

    // ✅ choose active preset here
    val keyHeight = KEY_HEIGHT_56
    val fontSize = FONT_22
    val spacing = SPACE_8
    val spaceWeight = SPACE_WEIGHT_MEDIUM

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {

        // -------- Row 1 (Numbers) --------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            listOf("1","2","3","4","5","6","7","8","9","0").forEach { key ->
                KeyBig(
                    label = key,
                    height = keyHeight,
                    fontSize = fontSize,
                    modifier = Modifier.weight(1f)
                ) {
                    onKeyPress(key)
                }
            }
        }

        // -------- Row 2 --------
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            listOf("Q","W","E","R","T","Y","U","I").forEach { key ->
                KeyBig(key, keyHeight, fontSize, Modifier.weight(1f)) {
                    onKeyPress(key)
                }
            }
        }

        // -------- Row 3 --------
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            listOf("A","S","D","F","G","H","J","K").forEach { key ->
                KeyBig(key, keyHeight, fontSize, Modifier.weight(1f)) {
                    onKeyPress(key)
                }
            }
        }

        // -------- Row 4 --------
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
            listOf("Z","X","C","V","B","N").forEach { key ->
                KeyBig(key, keyHeight, fontSize, Modifier.weight(1f)) {
                    onKeyPress(key)
                }
            }

            KeyBig("⌫", keyHeight, fontSize, Modifier.weight(1f)) {
                onBackspace()
            }
        }

        // -------- Bottom Row --------
        Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {

            KeyBig("CLEAR", keyHeight, fontSize, Modifier.weight(1.5f)) {
                onClear()
            }

            KeyBig("SPACE", keyHeight, fontSize, Modifier.weight(spaceWeight)) {
                onKeyPress(" ")
            }

            KeyBig("OK", keyHeight, fontSize, Modifier.weight(1.5f)) {
                onClose()
            }
        }
    }

}

@Composable
fun KeyBig(
    label: String,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(height),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text(label, fontSize = fontSize)
    }
}
