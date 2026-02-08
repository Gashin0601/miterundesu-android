package com.miterundesu.app.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.CameraManager
import com.miterundesu.app.ui.component.FooterView
import com.miterundesu.app.ui.component.HeaderView
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MainScreen(
    isTheaterMode: Boolean,
    scrollingMessage: String,
    images: List<CapturedImage>,
    zoomFactor: Float,
    isCapturing: Boolean,
    hideContent: Boolean,
    onTheaterToggle: () -> Unit,
    onExplanationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onScreenTap: () -> Unit,
    cameraPreview: @Composable (Modifier) -> Unit,
    shutterButton: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isTheaterMode) TheaterOrange else MainGreen

    var controlsVisible by remember { mutableStateOf(true) }
    var hideTimerKey by remember { mutableStateOf(0) }

    LaunchedEffect(isTheaterMode) {
        if (!isTheaterMode) {
            controlsVisible = true
        }
    }

    LaunchedEffect(isTheaterMode, hideTimerKey) {
        if (isTheaterMode) {
            delay(15_000L)
            controlsVisible = false
        }
    }

    fun resetTheaterTimer() {
        if (isTheaterMode) {
            controlsVisible = true
            hideTimerKey++
        }
    }

    if (hideContent) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .then(
                if (isTheaterMode) {
                    Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        resetTheaterTimer()
                        onScreenTap()
                    }
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                HeaderView(
                    isTheaterMode = isTheaterMode,
                    scrollingMessage = scrollingMessage,
                    onTheaterToggle = {
                        onTheaterToggle()
                        resetTheaterTimer()
                    },
                    onExplanationClick = onExplanationClick,
                    onSettingsClick = onSettingsClick
                )
            }

            cameraPreview(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
            )

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = controlsVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                FooterView(
                    images = images,
                    zoomFactor = zoomFactor,
                    isCapturing = isCapturing,
                    onShutterClick = {
                        onCaptureClick()
                        resetTheaterTimer()
                    },
                    onGalleryClick = onGalleryClick,
                    shutterButton = shutterButton
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
