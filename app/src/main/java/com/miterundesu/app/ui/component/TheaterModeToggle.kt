package com.miterundesu.app.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TheaterModeToggle(
    isTheaterMode: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityLabel: String = if (isTheaterMode) "Theater mode on" else "Theater mode off"
) {
    val rotation by animateFloatAsState(
        targetValue = if (isTheaterMode) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "theaterToggleRotation"
    )

    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 300),
        label = "theaterToggleAlpha"
    )

    Canvas(
        modifier = modifier
            .size(28.dp)
            .clickable(onClick = onToggle)
            .semantics { contentDescription = accessibilityLabel }
    ) {
        rotate(rotation) {
            if (isTheaterMode) {
                drawSunIcon(alpha)
            } else {
                drawMoonIcon(alpha)
            }
        }
    }
}

private fun DrawScope.drawMoonIcon(alpha: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.minDimension * 0.35f
    val color = Color.White.copy(alpha = alpha)

    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    drawCircle(
        color = Color.Black,
        radius = radius * 0.8f,
        center = Offset(centerX + radius * 0.4f, centerY - radius * 0.15f)
    )

    val star1X = centerX + radius * 0.9f
    val star1Y = centerY - radius * 0.8f
    drawStar(star1X, star1Y, radius * 0.12f, color)

    val star2X = centerX + radius * 1.1f
    val star2Y = centerY - radius * 0.2f
    drawStar(star2X, star2Y, radius * 0.08f, color)
}

private fun DrawScope.drawStar(cx: Float, cy: Float, r: Float, color: Color) {
    drawLine(
        color = color,
        start = Offset(cx - r, cy),
        end = Offset(cx + r, cy),
        strokeWidth = r * 0.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(cx, cy - r),
        end = Offset(cx, cy + r),
        strokeWidth = r * 0.5f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawSunIcon(alpha: Float) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val radius = size.minDimension * 0.22f
    val rayLength = size.minDimension * 0.13f
    val rayOffset = radius + size.minDimension * 0.06f
    val color = Color.White.copy(alpha = alpha)

    drawCircle(
        color = color,
        radius = radius,
        center = Offset(centerX, centerY)
    )

    val rayCount = 8
    for (i in 0 until rayCount) {
        val angle = (2.0 * PI * i / rayCount).toFloat()
        val startX = centerX + cos(angle) * rayOffset
        val startY = centerY + sin(angle) * rayOffset
        val endX = centerX + cos(angle) * (rayOffset + rayLength)
        val endY = centerY + sin(angle) * (rayOffset + rayLength)
        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = size.minDimension * 0.05f,
            cap = StrokeCap.Round
        )
    }
}
