package com.miterundesu.app.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

/**
 * ZoomableImage that accepts external scale/offset state so zoom buttons can control it.
 * Matching iOS ZoomableImageView which takes @Binding scale and @Binding offset.
 *
 * @param scale Current zoom scale (externally managed)
 * @param offset Current pan offset (externally managed)
 * @param onScaleChange Callback when scale changes (from pinch/double-tap)
 * @param onOffsetChange Callback when offset changes (from pan/pinch)
 * @param minScale Minimum zoom scale
 * @param maxScale Maximum zoom scale
 * @param onZoomChanged Callback for boolean zoomed state
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ZoomableImage(
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 10f,
    onZoomChanged: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var contentImageSize by remember { mutableStateOf(Size.Zero) }
    val animatedScale = remember { Animatable(scale) }
    val animatedOffset = remember { Animatable(offset, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    // Track last offset for drag gesture delta calculation
    var lastDragOffset by remember { mutableStateOf(offset) }

    // Sync animatables when external state changes (from zoom buttons)
    LaunchedEffect(scale, offset) {
        // Only snap if the animatable is not currently running an animation
        if (!animatedScale.isRunning) {
            animatedScale.snapTo(scale)
        }
        if (!animatedOffset.isRunning) {
            animatedOffset.snapTo(offset)
        }
        lastDragOffset = offset
    }

    fun calculateBoundedOffset(newOffset: Offset, currentScale: Float): Offset {
        if (currentScale <= 1f) return Offset.Zero

        val containerW = containerSize.width.toFloat()
        val containerH = containerSize.height.toFloat()
        if (containerW <= 0f || containerH <= 0f) return Offset.Zero

        val displayW: Float
        val displayH: Float
        if (contentImageSize.width > 0f && contentImageSize.height > 0f) {
            val imageAspect = contentImageSize.width / contentImageSize.height
            val viewAspect = containerW / containerH
            if (imageAspect > viewAspect) {
                displayW = containerW
                displayH = containerW / imageAspect
            } else {
                displayW = containerH * imageAspect
                displayH = containerH
            }
        } else {
            displayW = containerW
            displayH = containerH
        }

        val scaledW = displayW * currentScale
        val scaledH = displayH * currentScale

        val maxX = maxOf(0f, (scaledW - containerW) / 2f)
        val maxY = maxOf(0f, (scaledH - containerH) / 2f)

        return Offset(
            x = newOffset.x.coerceIn(-maxX, maxX),
            y = newOffset.y.coerceIn(-maxY, maxY)
        )
    }

    fun snapToBoundsIfNeeded() {
        if (scale <= 1f) return
        val bounded = calculateBoundedOffset(offset, scale)
        if (bounded != offset) {
            coroutineScope.launch {
                animatedOffset.animateTo(bounded, animationSpec = spring())
                onOffsetChange(bounded)
                lastDragOffset = bounded
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInteropFilter { false }
            .pointerInput(maxScale) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    val centerX = containerSize.width / 2f
                    val centerY = containerSize.height / 2f
                    val focusX = centroid.x - centerX
                    val focusY = centroid.y - centerY

                    val scaleFactor = newScale / scale
                    val newOffsetX = offset.x * scaleFactor + focusX * (1 - scaleFactor)
                    val newOffsetY = offset.y * scaleFactor + focusY * (1 - scaleFactor)

                    val rawOffset = Offset(newOffsetX + pan.x, newOffsetY + pan.y)
                    // During gesture: allow overflow (matching iOS)
                    onOffsetChange(rawOffset)
                    onScaleChange(newScale)
                    onZoomChanged?.invoke(newScale > 1.01f)
                    lastDragOffset = rawOffset

                    coroutineScope.launch {
                        animatedScale.snapTo(newScale)
                        animatedOffset.snapTo(rawOffset)
                    }
                }
                // After gesture ends, snap to bounds
                snapToBoundsIfNeeded()
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            launch {
                                animatedScale.animateTo(1f, animationSpec = spring())
                                onScaleChange(1f)
                                onZoomChanged?.invoke(false)
                            }
                            launch {
                                animatedOffset.animateTo(Offset.Zero, animationSpec = spring())
                                onOffsetChange(Offset.Zero)
                                lastDragOffset = Offset.Zero
                            }
                        }
                    },
                    onLongPress = {
                        // Block long press (matching iOS)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    translationX = animatedOffset.value.x
                    translationY = animatedOffset.value.y
                }
                .onSizeChanged { size ->
                    contentImageSize = Size(size.width.toFloat(), size.height.toFloat())
                }
        ) {
            content()
        }
    }
}
