package com.miterundesu.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miterundesu.app.manager.LocalizationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Zoom controls matching iOS behavior:
 * - Zoom in/out buttons with tap (1.5x step) and long-press (continuous zoom with acceleration)
 * - 1x reset button with long-press "peek" (zooms to 1x while held, restores on release)
 *
 * IMPORTANT: All values used inside pointerInput/LaunchedEffect are accessed via
 * rememberUpdatedState to avoid stale closure captures.
 */
@Composable
fun ZoomControls(
    currentZoom: Float,
    maxZoom: Float,
    onZoomChange: (Float) -> Unit,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    currentOffset: Offset = Offset.Zero,
    onOffsetChange: (Offset) -> Unit = {},
    onSmoothZoomChange: (target: Float, durationSeconds: Float) -> Unit = { target, _ -> onZoomChange(target) }
) {
    val buttonSize = screenWidth * 0.11f
    val iconSize = screenWidth * 0.05f
    val buttonSpacing = screenWidth * 0.03f

    // rememberUpdatedState keeps latest values accessible inside long-lived coroutines
    val latestZoom by rememberUpdatedState(currentZoom)
    val latestMaxZoom by rememberUpdatedState(maxZoom)
    val latestOffset by rememberUpdatedState(currentOffset)
    val latestOnZoomChange by rememberUpdatedState(onZoomChange)
    val latestOnOffsetChange by rememberUpdatedState(onOffsetChange)
    val latestOnSmoothZoomChange by rememberUpdatedState(onSmoothZoomChange)

    var isZoomInPressed by remember { mutableStateOf(false) }
    var isZoomOutPressed by remember { mutableStateOf(false) }
    var savedZoom by remember { mutableFloatStateOf(1f) }
    var savedOffset by remember { mutableStateOf(Offset.Zero) }

    // Continuous zoom in with iOS-matching acceleration algorithm
    LaunchedEffect(isZoomInPressed) {
        if (!isZoomInPressed) return@LaunchedEffect
        delay(500L)
        if (!isZoomInPressed) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        while (isZoomInPressed) {
            val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
            val zoom = latestZoom // always reads latest value

            val baseStep = 0.03f
            val timeAcceleration = (1.0 + min(elapsed / 2.0, 1.0).pow(1.5) * 3.0).toFloat()
            val zoomMultiplier = max(1.0f, sqrt(zoom / 10.0f))
            val step = baseStep * timeAcceleration * zoomMultiplier

            val newScale = min(zoom + step, latestMaxZoom)
            val scaleDiff = newScale / zoom

            val newOffset = Offset(
                x = latestOffset.x * scaleDiff,
                y = latestOffset.y * scaleDiff
            )
            latestOnOffsetChange(newOffset)
            latestOnZoomChange(newScale)

            if (newScale >= latestMaxZoom) break
            delay(30L)
        }
    }

    // Continuous zoom out with iOS-matching acceleration algorithm
    LaunchedEffect(isZoomOutPressed) {
        if (!isZoomOutPressed) return@LaunchedEffect
        delay(500L)
        if (!isZoomOutPressed) return@LaunchedEffect
        val startTime = System.currentTimeMillis()
        while (isZoomOutPressed) {
            val elapsed = (System.currentTimeMillis() - startTime) / 1000.0
            val zoom = latestZoom

            val baseStep = 0.03f
            val timeAcceleration = (1.0 + min(elapsed / 2.0, 1.0).pow(1.5) * 3.0).toFloat()
            val zoomMultiplier = max(1.0f, sqrt(zoom / 10.0f))
            val step = baseStep * timeAcceleration * zoomMultiplier
            val outStep = step * 0.7f

            val newScale = max(zoom - outStep, 1.0f)

            if (newScale <= 1.0f) {
                latestOnZoomChange(1.0f)
                latestOnOffsetChange(Offset.Zero)
                break
            } else {
                val scaleDiff = newScale / zoom
                val newOffset = Offset(
                    x = latestOffset.x * scaleDiff,
                    y = latestOffset.y * scaleDiff
                )
                latestOnOffsetChange(newOffset)
                latestOnZoomChange(newScale)
            }

            delay(30L)
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Zoom in button
        ZoomCircleButton(
            buttonSize = buttonSize,
            onPress = { isZoomInPressed = true },
            onRelease = { isZoomInPressed = false },
            onTap = {
                // Single tap: zoom in by 1.5x (matching iOS zoomIn())
                val zoom = latestZoom
                val newScale = (zoom * 1.5f).coerceIn(1f, latestMaxZoom)
                val scaleDiff = newScale / zoom
                val newOffset = Offset(
                    x = latestOffset.x * scaleDiff,
                    y = latestOffset.y * scaleDiff
                )
                latestOnOffsetChange(newOffset)
                latestOnZoomChange(newScale)
            },
            accessibilityLabel = LocalizationManager.localizedString("zoom_in"),
            accessibilityHint = LocalizationManager.localizedString("zoom_in_hint")
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        // Zoom out button
        ZoomCircleButton(
            buttonSize = buttonSize,
            onPress = { isZoomOutPressed = true },
            onRelease = { isZoomOutPressed = false },
            onTap = {
                // Single tap: zoom out by 1/1.5x (matching iOS zoomOut())
                val zoom = latestZoom
                val newScale = (zoom / 1.5f).coerceIn(1f, latestMaxZoom)
                if (newScale <= 1.0f) {
                    latestOnZoomChange(1.0f)
                    latestOnOffsetChange(Offset.Zero)
                } else {
                    val scaleDiff = newScale / zoom
                    val newOffset = Offset(
                        x = latestOffset.x * scaleDiff,
                        y = latestOffset.y * scaleDiff
                    )
                    latestOnOffsetChange(newOffset)
                    latestOnZoomChange(newScale)
                }
            },
            accessibilityLabel = LocalizationManager.localizedString("zoom_out"),
            accessibilityHint = LocalizationManager.localizedString("zoom_out_hint")
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }

        // 1x reset button with long-press "peek" behavior (matching iOS)
        // Uses awaitEachGesture instead of detectTapGestures to avoid onPress/onTap conflict.
        // iOS uses onLongPressGesture(minimumDuration: 0.3, pressing:perform:) which handles
        // everything in a single callback. We replicate this with withTimeoutOrNull(300ms).
        Box(
            modifier = Modifier
                .size(buttonSize)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()

                        // Save current state on press (matching iOS pressing=true branch)
                        val wasAbove1x = latestZoom > 1.0f
                        if (wasAbove1x) {
                            savedZoom = latestZoom
                            savedOffset = latestOffset
                        }

                        // Wait up to 300ms for release (matching iOS minimumDuration: 0.3)
                        val up = withTimeoutOrNull(300L) {
                            waitForUpOrCancellation()
                        }

                        if (up != null) {
                            // Released within 300ms → TAP: smooth reset to 1x (iOS duration: 0.2)
                            up.consume()
                            latestOnSmoothZoomChange(1f, 0.2f)
                            latestOnOffsetChange(Offset.Zero)
                        } else {
                            // 300ms elapsed, finger still down → LONG PRESS "peek"
                            // Snap to 1x with fast animation (iOS duration: 0.08)
                            latestOnSmoothZoomChange(1f, 0.08f)
                            latestOnOffsetChange(Offset.Zero)

                            // Wait for finger release
                            val release = waitForUpOrCancellation()
                            release?.consume()

                            // Restore saved zoom on release (iOS duration: 0.08)
                            if (wasAbove1x) {
                                latestOnSmoothZoomChange(savedZoom, 0.08f)
                                latestOnOffsetChange(savedOffset)
                            }
                        }
                    }
                }
                .semantics {
                    contentDescription = LocalizationManager.localizedString("zoom_reset") +
                        ", " + LocalizationManager.localizedString("zoom_reset_hint")
                    role = Role.Button
                },
            contentAlignment = Alignment.Center
        ) {
            // "1.circle" icon matching iOS SF Symbol
            OneCircleIcon(
                size = iconSize,
                modifier = Modifier.clearAndSetSemantics { }
            )
        }
    }
}

@Composable
private fun ZoomCircleButton(
    buttonSize: Dp,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onTap: () -> Unit,
    accessibilityLabel: String,
    accessibilityHint: String,
    content: @Composable () -> Unit
) {
    // Keep latest callback references for use inside pointerInput(Unit)
    val latestOnPress by rememberUpdatedState(onPress)
    val latestOnRelease by rememberUpdatedState(onRelease)
    val latestOnTap by rememberUpdatedState(onTap)

    Box(
        modifier = Modifier
            .size(buttonSize)
            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        latestOnPress()
                        try {
                            tryAwaitRelease()
                        } finally {
                            latestOnRelease()
                        }
                    },
                    onTap = {
                        latestOnTap()
                    }
                )
            }
            .semantics {
                contentDescription = accessibilityLabel + ", " + accessibilityHint
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Custom "1.circle" icon matching iOS SF Symbol.
 * Draws a white circle outline with "1" text centered inside.
 */
@Composable
private fun OneCircleIcon(
    size: Dp,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fontSize = with(density) { (size * 0.55f).toSp() }
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = fontSize,
        fontWeight = FontWeight.Medium
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.width / 2f

        // Circle outline (matching iOS 1.circle thin stroke)
        drawCircle(
            color = Color.White,
            radius = radius - 1.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx())
        )

        // "1" text centered
        val textLayoutResult = textMeasurer.measure("1", textStyle)
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = center.x - textLayoutResult.size.width / 2f,
                y = center.y - textLayoutResult.size.height / 2f
            )
        )
    }
}
