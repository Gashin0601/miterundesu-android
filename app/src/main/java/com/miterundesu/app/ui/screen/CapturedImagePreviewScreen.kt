package com.miterundesu.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.PressModeManager
import com.miterundesu.app.manager.SecurityManager
import com.miterundesu.app.manager.SettingsManager
import com.miterundesu.app.ui.component.RecordingWarningBanner
import com.miterundesu.app.ui.component.ScreenshotWarningDialog
import com.miterundesu.app.ui.component.TimeRemainingBadge
import com.miterundesu.app.ui.component.WatermarkView
import com.miterundesu.app.ui.component.ZoomControls
import com.miterundesu.app.ui.component.ZoomableImage
import kotlinx.coroutines.delay

@Composable
fun CapturedImagePreviewScreen(
    image: CapturedImage,
    securityManager: SecurityManager,
    settingsManager: SettingsManager,
    pressModeManager: PressModeManager,
    localizationManager: LocalizationManager,
    onClose: () -> Unit,
    onExplanation: () -> Unit,
    onSettings: () -> Unit
) {
    val isRecording by securityManager.isRecording.collectAsStateWithLifecycle()
    val hideContent by securityManager.hideContent.collectAsStateWithLifecycle()
    val showScreenshotWarning by securityManager.showScreenshotWarning.collectAsStateWithLifecycle()
    val isPressModeEnabled by pressModeManager.isPressModeEnabled.collectAsStateWithLifecycle()
    val isTheaterMode by settingsManager.isTheaterMode.collectAsStateWithLifecycle()
    var currentZoom by remember { mutableFloatStateOf(1f) }
    var showDeletedScreen by remember { mutableStateOf(false) }

    // Auto-close if image expires
    LaunchedEffect(image.id) {
        while (true) {
            delay(1000L)
            if (image.isExpired) {
                showDeletedScreen = true
                break
            }
        }
    }

    // Check expiry on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (image.isExpired) {
                    showDeletedScreen = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val shouldBlur = !isPressModeEnabled && (hideContent || isRecording)

    if (showDeletedScreen) {
        ImageDeletedScreen(
            localizationManager = localizationManager,
            onDismiss = {
                showDeletedScreen = false
                onClose()
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Zoomable image
        ZoomableImage(
            modifier = if (shouldBlur) {
                Modifier.fillMaxSize().blur(30.dp)
            } else {
                Modifier.fillMaxSize()
            }
        ) {
            val bitmap = remember(image.id) {
                BitmapFactory.decodeByteArray(image.imageData, 0, image.imageData.size)
            }
            if (bitmap != null && !bitmap.isRecycled) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = localizationManager.localizedString("accessibility_capture_preview"),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Watermark overlay
        WatermarkView(
            modifier = Modifier.align(Alignment.BottomStart)
        )

        // Zoom controls
        ZoomControls(
            currentZoom = currentZoom,
            maxZoom = 10f,
            onZoomChange = { currentZoom = it },
            onSmoothZoom = { target, _ -> currentZoom = target },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Top controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Time remaining badge (center)
            TimeRemainingBadge(
                image = image,
                modifier = Modifier.align(Alignment.Center)
            )

            // Explanation button (left of settings, normal mode only)
            if (!isTheaterMode) {
                IconButton(
                    onClick = onExplanation,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = localizationManager.localizedString("accessibility_explanation_button"),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Settings button (right)
            IconButton(
                onClick = onSettings,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = localizationManager.localizedString("accessibility_settings_button"),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Close button (styled like ShutterButton with X icon)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
                .size(72.dp)
                .border(3.dp, Color.White, CircleShape)
                .padding(3.dp)
                .background(Color.White, CircleShape)
                .clickable(onClick = onClose)
                .semantics {
                    contentDescription = localizationManager.localizedString("close")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
        }

        // Security warnings
        if (!isPressModeEnabled) {
            ScreenshotWarningDialog(
                visible = showScreenshotWarning,
                title = localizationManager.localizedString("screenshot_warning_title"),
                message = localizationManager.localizedString("screenshot_warning_message"),
                onDismiss = { /* managed by SecurityManager timer */ }
            )

            RecordingWarningBanner(
                visible = isRecording,
                message = localizationManager.localizedString("recording_detected"),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
