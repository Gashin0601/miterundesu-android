package com.miterundesu.app.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager

@Composable
fun InfiniteScrollingText(
    text: String,
    modifier: Modifier = Modifier
) {
    if (text.isBlank()) return

    val density = LocalDensity.current
    val spacingPx = with(density) { 40.dp.toPx() }
    var textWidthPx by remember { mutableIntStateOf(0) }

    val itemWidthPx = textWidthPx + spacingPx.toInt()
    val totalDistance = itemWidthPx * 10f
    val speedPx = with(density) { 50.dp.toPx() } // 50dp/sec matching iOS 50pt/sec
    val durationMs = if (totalDistance > 0) (totalDistance / speedPx * 1000f).toInt() else 3000

    val infiniteTransition = rememberInfiniteTransition(label = "scrollText")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -totalDistance,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scrollOffset"
    )

    Row(
        modifier = modifier
            .height(32.dp)
            .clipToBounds()
            .offset { IntOffset(offset.toInt(), 0) }
            .clearAndSetSemantics {
                contentDescription = "${LocalizationManager.localizedString("scrolling_message_label")}\u3001$text"
            },
        horizontalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        repeat(20) {
            Text(
                text = text,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = if (it == 0) {
                    Modifier.onSizeChanged { size -> textWidthPx = size.width }
                } else {
                    Modifier
                }
            )
        }
    }
}
