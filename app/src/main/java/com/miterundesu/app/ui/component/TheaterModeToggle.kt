package com.miterundesu.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange

@Composable
fun TheaterModeToggle(
    isTheaterMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = LocalizationManager.localizedString(
        if (isTheaterMode) "switch_to_normal_mode" else "switch_to_theater_mode"
    )
    val hint = LocalizationManager.localizedString(
        if (isTheaterMode) "switch_to_normal_hint" else "switch_to_theater_hint"
    )
    val theaterText = LocalizationManager.localizedString("theater_mode")

    Row(
        modifier = modifier
            .background(
                Color.White.copy(alpha = 0.25f),
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics {
                contentDescription = label + ", " + hint
            },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom split-circle icon (matching iOS TheaterModeIcon)
        TheaterModeIcon(
            isTheaterMode = isTheaterMode,
            modifier = Modifier.size(18.dp)
        )

        // Text label
        Text(
            text = theaterText,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun TheaterModeIcon(
    isTheaterMode: Boolean,
    modifier: Modifier = Modifier
) {
    val upperLeftColor = if (isTheaterMode) MainGreen else TheaterOrange
    val lowerRightColor = if (isTheaterMode) TheaterOrange else MainGreen
    val iconColor = if (isTheaterMode) TheaterOrange else MainGreen

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Split circle background drawn with Canvas
        Canvas(
            modifier = Modifier
                .size(18.dp)
                .clearAndSetSemantics { }
        ) {
            val size = size.width
            val center = Offset(size / 2f, size / 2f)
            val radius = size / 2f

            // Circle clip path
            val circlePath = Path().apply {
                addOval(
                    androidx.compose.ui.geometry.Rect(
                        center = center,
                        radius = radius
                    )
                )
            }

            // White circle background
            drawCircle(
                color = Color.White,
                radius = radius,
                center = center
            )

            // Upper-left triangle (top-left to top-right to bottom-left)
            val upperLeftPath = Path().apply {
                moveTo(0f, 0f)
                lineTo(size, 0f)
                lineTo(0f, size)
                close()
            }
            clipPath(circlePath) {
                drawPath(
                    path = upperLeftPath,
                    color = upperLeftColor
                )
            }

            // Lower-right triangle (top-right to bottom-right to bottom-left)
            val lowerRightPath = Path().apply {
                moveTo(size, 0f)
                lineTo(size, size)
                lineTo(0f, size)
                close()
            }
            clipPath(circlePath) {
                drawPath(
                    path = lowerRightPath,
                    color = lowerRightColor
                )
            }

            // White diagonal border line (matching iOS lineWidth: 1.2)
            clipPath(circlePath) {
                drawLine(
                    color = Color.White,
                    start = Offset(0f, 0f),
                    end = Offset(size, size),
                    strokeWidth = 1.2.dp.toPx(),
                    cap = StrokeCap.Butt
                )
            }

            // Thin white circle border (matching iOS 0.3 opacity, 0.8pt)
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = radius - 0.4.dp.toPx(),
                center = center,
                style = Stroke(width = 0.8.dp.toPx())
            )
        }

        // Center icon with white outline effect (matching iOS 4-offset approach)
        val iconSize = 9.dp
        val outlineOffset = 0.4.dp

        // White outline copies (4 directions)
        Icon(
            imageVector = if (isTheaterMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(iconSize)
                .clearAndSetSemantics { }
        )

        // Main colored icon on top
        Icon(
            imageVector = if (isTheaterMode) Icons.Default.DarkMode else Icons.Default.LightMode,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(iconSize - 1.dp)
                .clearAndSetSemantics { }
        )
    }
}
