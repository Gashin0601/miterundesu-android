package com.miterundesu.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.pow

@Composable
fun ZoomControls(
    currentZoom: Float,
    maxZoom: Float,
    onZoomChange: (Float) -> Unit,
    onSmoothZoom: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var isZoomInPressed by remember { mutableStateOf(false) }
    var isZoomOutPressed by remember { mutableStateOf(false) }
    var is1xPressed by remember { mutableStateOf(false) }
    var savedZoom by remember { mutableFloatStateOf(1f) }
    var pressStartTime by remember { mutableLongStateOf(0L) }
    var isLongPress1x by remember { mutableStateOf(false) }

    // Continuous zoom in
    LaunchedEffect(isZoomInPressed) {
        if (!isZoomInPressed) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        while (isZoomInPressed) {
            val elapsed = (System.currentTimeMillis() - startTime) / 1000f
            val acceleration = (1.0 + elapsed).pow(1.5).toFloat()
            val zoomSpeed = 0.02f * acceleration * (1f + currentZoom * 0.01f)
            val newZoom = (currentZoom + zoomSpeed).coerceIn(1f, maxZoom)
            onZoomChange(newZoom)
            delay(30L)
        }
    }

    // Continuous zoom out
    LaunchedEffect(isZoomOutPressed) {
        if (!isZoomOutPressed) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        while (isZoomOutPressed) {
            val elapsed = (System.currentTimeMillis() - startTime) / 1000f
            val acceleration = (1.0 + elapsed).pow(1.5).toFloat()
            val zoomSpeed = 0.02f * acceleration * (1f + currentZoom * 0.01f)
            val newZoom = (currentZoom - zoomSpeed).coerceIn(1f, maxZoom)
            onZoomChange(newZoom)
            delay(30L)
        }
    }

    Row(
        modifier = modifier
            .background(
                Color.Black.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Zoom out button
        ZoomButton(
            onPress = { isZoomOutPressed = true },
            onRelease = { isZoomOutPressed = false },
            onTap = {
                val newZoom = (currentZoom - 1f).coerceIn(1f, maxZoom)
                onZoomChange(newZoom)
            },
            contentDescription = "縮小"
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 1x reset button
        Box(
            modifier = Modifier
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            pressStartTime = System.currentTimeMillis()
                            is1xPressed = true
                            isLongPress1x = false
                            savedZoom = currentZoom

                            val longPressTimeout = 300L
                            try {
                                val startTime = System.currentTimeMillis()
                                while (is1xPressed) {
                                    delay(10L)
                                    if (System.currentTimeMillis() - startTime > longPressTimeout && !isLongPress1x) {
                                        isLongPress1x = true
                                        onSmoothZoom(1f, 0.2f)
                                        break
                                    }
                                }
                                tryAwaitRelease()
                            } finally {
                                is1xPressed = false
                                if (isLongPress1x) {
                                    // Restore saved zoom on release
                                    onSmoothZoom(savedZoom, 0.2f)
                                    isLongPress1x = false
                                }
                            }
                        },
                        onTap = {
                            // Normal tap: reset to 1x permanently
                            onSmoothZoom(1f, 0.3f)
                        }
                    )
                }
                .semantics { contentDescription = "リセット" },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "1x",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Zoom in button
        ZoomButton(
            onPress = { isZoomInPressed = true },
            onRelease = { isZoomInPressed = false },
            onTap = {
                val newZoom = (currentZoom + 1f).coerceIn(1f, maxZoom)
                onZoomChange(newZoom)
            },
            contentDescription = "拡大"
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ZoomButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onTap: () -> Unit,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(
                Color.White.copy(alpha = if (isPressed) 0.3f else 0.15f),
                RoundedCornerShape(20.dp)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            onRelease()
                        }
                    },
                    onTap = {
                        onTap()
                    }
                )
            }
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
