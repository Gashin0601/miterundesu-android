package com.miterundesu.app.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.SettingsManager
import com.miterundesu.app.ui.component.FooterView
import com.miterundesu.app.ui.component.HeaderView
import com.miterundesu.app.ui.component.RecordingWarningBanner
import com.miterundesu.app.ui.component.ScreenshotWarningDialog
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    isTheaterMode: Boolean,
    scrollingMessage: String,
    images: List<CapturedImage>,
    zoomFactor: Float,
    isCapturing: Boolean,
    isCameraReady: Boolean = true,
    hideContent: Boolean,
    isRecording: Boolean,
    isPressModeEnabled: Boolean = false,
    showScreenshotWarning: Boolean = false,
    showRecordingWarning: Boolean = false,
    onTheaterToggle: () -> Unit,
    onExplanationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onCaptureClick: () -> Unit,
    onScreenTap: () -> Unit,
    onDismissScreenshotWarning: () -> Unit = {},
    localizationManager: com.miterundesu.app.manager.LocalizationManager = com.miterundesu.app.manager.LocalizationManager,
    settingsManager: SettingsManager? = null,
    cameraPreview: @Composable (Modifier) -> Unit,
    shutterButton: @Composable (Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isTheaterMode) TheaterOrange else MainGreen
    val view = LocalView.current

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

    // TalkBack announcements for capture events (matching iOS VoiceOver)
    var wasCapturing by remember { mutableStateOf(false) }
    LaunchedEffect(isCapturing) {
        if (isCapturing && !wasCapturing) {
            view.announceForAccessibility(
                localizationManager.localizedString("capture_started")
            )
        } else if (!isCapturing && wasCapturing) {
            view.announceForAccessibility(
                localizationManager.localizedString("capture_complete")
            )
        }
        wasCapturing = isCapturing
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

    Box(modifier = modifier.fillMaxSize()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .then(
                    if (isTheaterMode) {
                        Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                resetTheaterTimer()
                                onScreenTap()
                            }
                            .semantics {
                                // Matching iOS: accessibilityLabel/hint for theater mode tap area
                                val theaterLabel = if (!controlsVisible) {
                                    LocalizationManager.localizedString("show_ui")
                                } else {
                                    LocalizationManager.localizedString("switch_to_normal_mode")
                                }
                                val theaterHint = if (!controlsVisible) {
                                    LocalizationManager.localizedString("show_ui_hint")
                                } else {
                                    LocalizationManager.localizedString("switch_to_normal_hint")
                                }
                                contentDescription = theaterLabel + ", " + theaterHint
                                role = Role.Button
                            }
                    } else {
                        Modifier
                    }
                )
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // Animate alpha for controls (matching iOS opacity fade - preserves layout space)
            val controlsAlpha by animateFloatAsState(
                targetValue = if (controlsVisible) 1f else 0f,
                animationSpec = tween(durationMillis = 300),
                label = "controlsAlpha"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                // Header: always in layout, fade via graphicsLayer (matching iOS opacity behavior)
                Box(modifier = Modifier.graphicsLayer { alpha = controlsAlpha }) {
                    HeaderView(
                        isTheaterMode = isTheaterMode,
                        scrollingMessage = scrollingMessage,
                        onTheaterToggle = {
                            onTheaterToggle()
                            resetTheaterTimer()
                        },
                        onExplanationClick = onExplanationClick,
                        onSettingsClick = onSettingsClick,
                        controlsVisible = controlsVisible
                    )
                }

                cameraPreview(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = screenWidth * 0.031f,
                            vertical = screenHeight * 0.009f
                        )
                        .aspectRatio(3f / 4f)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Footer: always in layout, fade via graphicsLayer (matching iOS opacity behavior)
                Box(modifier = Modifier.graphicsLayer { alpha = controlsAlpha }) {
                    FooterView(
                        images = images,
                        zoomFactor = zoomFactor,
                        isCapturing = isCapturing,
                        isTheaterMode = isTheaterMode,
                        hideContent = hideContent,
                        isRecording = isRecording,
                        onShutterClick = {
                            onCaptureClick()
                            resetTheaterTimer()
                        },
                        onGalleryClick = onGalleryClick,
                        screenWidth = screenWidth,
                        shutterButton = shutterButton
                    )
                }
            }
        }

        // Issue 5: Camera recording warning overlay (matching iOS ContentView lines 211-229)
        if (isRecording && !isPressModeEnabled) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(32.dp)
                        .semantics(mergeDescendants = true) { } // Matching iOS accessibilityElement(children: .combine)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(80.dp)
                            .clearAndSetSemantics { } // Decorative icon (matching iOS accessibilityHidden)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = localizationManager.localizedString("screen_recording_warning"),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizationManager.localizedString("no_recording_message"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Issue 2: Screenshot warning dialog (matching iOS ContentView lines 286-298)
        if (!isPressModeEnabled && showScreenshotWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onDismissScreenshotWarning() }
            )
            ScreenshotWarningDialog(
                visible = showScreenshotWarning,
                title = localizationManager.localizedString("screenshot_detected"),
                message = localizationManager.localizedString("screenshot_warning_message"),
                onDismiss = onDismissScreenshotWarning
            )
        }

        // Issue 2: Recording warning banner at top (matching iOS ContentView lines 274-283)
        if (!isPressModeEnabled) {
            RecordingWarningBanner(
                visible = showRecordingWarning,
                title = localizationManager.localizedString("screen_recording_detected"),
                message = localizationManager.localizedString("screen_recording_warning_message"),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
