package com.miterundesu.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import java.util.Locale

@Composable
fun ZoomLevelView(
    zoomFactor: Float,
    modifier: Modifier = Modifier
) {
    val displayText = "\u00D7${String.format(Locale.US, "%.1f", zoomFactor)}"
    val accessibilityLabel = LocalizationManager.localizedString(
        "current_zoom_accessibility",
        "zoom" to String.format(Locale.US, "%.1f", zoomFactor)
    )

    Text(
        text = displayText,
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .background(
                Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                contentDescription = accessibilityLabel
            }
    )
}
