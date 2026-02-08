package com.miterundesu.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.ui.theme.MainGreen
import kotlinx.coroutines.delay

@Composable
fun TimeRemainingBadge(
    image: CapturedImage?,
    modifier: Modifier = Modifier
) {
    if (image == null) return

    var remainingMs by remember(image.id) { mutableLongStateOf(image.remainingTime) }

    LaunchedEffect(image.id) {
        while (true) {
            delay(1000L)
            remainingMs = image.remainingTime
            if (remainingMs <= 0L) break
        }
    }

    val minutes = (remainingMs / 60000).toInt()
    val seconds = ((remainingMs % 60000) / 1000).toInt()
    val isLowTime = remainingMs < 120_000L

    val textColor = if (isLowTime) Color.Red else MainGreen
    val displayText = "${minutes}m ${seconds}s"

    Text(
        text = displayText,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 12.sp,
        modifier = modifier
            .background(
                Color.White,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
