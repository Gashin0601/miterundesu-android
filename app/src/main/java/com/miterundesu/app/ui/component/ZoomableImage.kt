package com.miterundesu.app.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.launch

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    minScale: Float = 1f,
    maxScale: Float = 10f,
    onZoomChanged: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val animatedScale = remember { Animatable(1f) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val coroutineScope = rememberCoroutineScope()

    fun calculateBoundedOffset(newOffset: Offset, currentScale: Float): Offset {
        if (currentScale <= 1f) return Offset.Zero
        val maxX = (containerSize.width * (currentScale - 1f)) / 2f
        val maxY = (containerSize.height * (currentScale - 1f)) / 2f
        return Offset(
            x = newOffset.x.coerceIn(-maxX, maxX),
            y = newOffset.y.coerceIn(-maxY, maxY)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(minScale, maxScale)

                    // Anchor-point zoom: adjust offset so zoom is centered on pinch location
                    val centerX = containerSize.width / 2f
                    val centerY = containerSize.height / 2f
                    val focusX = centroid.x - centerX
                    val focusY = centroid.y - centerY

                    val scaleFactor = newScale / scale
                    val newOffsetX = offset.x * scaleFactor + focusX * (1 - scaleFactor)
                    val newOffsetY = offset.y * scaleFactor + focusY * (1 - scaleFactor)

                    val rawOffset = Offset(newOffsetX + pan.x, newOffsetY + pan.y)
                    offset = calculateBoundedOffset(rawOffset, newScale)
                    scale = newScale
                    onZoomChanged?.invoke(newScale > 1.01f)

                    coroutineScope.launch {
                        animatedScale.snapTo(scale)
                        animatedOffset.snapTo(offset)
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        coroutineScope.launch {
                            launch {
                                animatedScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring()
                                )
                                scale = 1f
                                onZoomChanged?.invoke(false)
                            }
                            launch {
                                animatedOffset.animateTo(
                                    targetValue = Offset.Zero,
                                    animationSpec = spring()
                                )
                                offset = Offset.Zero
                            }
                        }
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
        ) {
            content()
        }
    }
}
