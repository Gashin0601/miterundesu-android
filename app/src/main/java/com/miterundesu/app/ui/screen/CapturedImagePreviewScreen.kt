package com.miterundesu.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.ImageManager
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
import com.miterundesu.app.ui.theme.MainGreen
import kotlinx.coroutines.delay

@Composable
fun CapturedImagePreviewScreen(
    image: CapturedImage,
    imageManager: ImageManager? = null,
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
    val maxZoomFactor by settingsManager.maxZoomFactor.collectAsStateWithLifecycle()
    var currentScale by remember { mutableFloatStateOf(1f) }
    var currentOffset by remember { mutableStateOf(Offset.Zero) }
    var showDeletedScreen by remember { mutableStateOf(false) }
    var wasInBackground by remember { mutableStateOf(false) }

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val horizontalPadding = screenWidth * 0.05f
    val verticalPadding = screenWidth * 0.01f
    val buttonSize = (screenWidth * 0.11f).dp
    val closeButtonSize = (screenWidth * 0.18f).dp

    // Auto-close if image expires (matching iOS per-second timer)
    LaunchedEffect(image.id) {
        while (true) {
            delay(1000L)
            imageManager?.removeExpiredImages()
            if (image.isExpired) {
                if (wasInBackground) {
                    onClose()
                } else {
                    showDeletedScreen = true
                }
                break
            }
        }
    }

    // Check expiry on resume (matching iOS background-aware lifecycle)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    wasInBackground = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    if (image.isExpired) {
                        if (wasInBackground) {
                            onClose()
                        } else {
                            showDeletedScreen = true
                        }
                    }
                    wasInBackground = false
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // iOS: hideContent -> full black overlay, isRecording -> blur(50)
    val shouldShowBlackOverlay = !isPressModeEnabled && hideContent
    val shouldBlur = !isPressModeEnabled && isRecording

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
            .background(MainGreen) // Background: MainGreen (not black, matching iOS)
    ) {
        // hideContent: full black overlay (matching iOS securityManager.hideContent)
        if (shouldShowBlackOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        } else {
            // Zoomable image (connected to shared zoom state, matching iOS)
            ZoomableImage(
                scale = currentScale,
                offset = currentOffset,
                onScaleChange = { newScale ->
                    currentScale = newScale
                },
                onOffsetChange = { newOffset ->
                    currentOffset = newOffset
                },
                maxScale = maxZoomFactor,
                modifier = if (shouldBlur) {
                    Modifier
                        .fillMaxSize()
                        .blur(50.dp) // blur radius 50 (matching iOS)
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
        }

        // Watermark overlay (bottom-left, matching iOS)
        WatermarkView(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = (horizontalPadding * 0.6f).dp, bottom = (verticalPadding * 1.2f).dp)
        )

        // Screen recording warning overlay (matching iOS)
        if (isRecording && !isPressModeEnabled) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(horizontal = (screenWidth * 0.1f).dp)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(32.dp)
                        .semantics(mergeDescendants = true) { } // Matching iOS accessibilityElement(children: .combine)
                ) {
                    // eye.slash.fill equivalent (matching iOS)
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

        // Top bar: remaining time LEFT, explanation CENTER, settings RIGHT (matching iOS layout)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = horizontalPadding.dp, end = horizontalPadding.dp)
                .align(Alignment.TopCenter)
        ) {
            // Left: remaining time badge (matching iOS position)
            TimeRemainingBadge(
                image = image,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // Center: Explanation button (visible in ALL modes, matching iOS)
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .clickable(onClick = onExplanation)
                    .padding(horizontal = (horizontalPadding * 0.8f).dp, vertical = (verticalPadding * 0.8f).dp)
                    .semantics { contentDescription = localizationManager.localizedString("explanation") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = null,
                    tint = MainGreen,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = localizationManager.localizedString("explanation"),
                    color = MainGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Right: Settings button (pill style, matching iOS)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onSettings)
                    .padding(horizontal = (horizontalPadding * 0.6f).dp, vertical = (verticalPadding * 0.6f).dp)
                    .semantics { contentDescription = localizationManager.localizedString("settings") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = localizationManager.localizedString("settings"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Zoom controls + zoom level display: vertical column on bottom-RIGHT (matching iOS)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (horizontalPadding * 0.6f).dp, bottom = (verticalPadding * 1.2f).dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy((screenWidth * 0.11f * 0.18f).dp)
        ) {
            // ZoomControls component wired to shared state (matching iOS)
            ZoomControls(
                currentZoom = currentScale,
                currentOffset = currentOffset,
                maxZoom = maxZoomFactor,
                onZoomChange = { newScale ->
                    currentScale = newScale
                },
                onOffsetChange = { newOffset ->
                    currentOffset = newOffset
                },
                screenWidth = screenWidth.dp
            )

            // Zoom level display - always visible at bottom-right (matching iOS)
            Text(
                text = "\u00D7${String.format("%.1f", currentScale)}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(horizontal = (horizontalPadding * 0.6f).dp, vertical = 4.dp)
            )
        }

        // Close button: large circle at bottom center (matching iOS proportions)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (screenWidth * 0.025f).dp)
                .size(closeButtonSize)
                .border((closeButtonSize * 0.057f), Color.White, CircleShape)
                .padding((closeButtonSize * 0.057f))
                .background(Color.White, CircleShape)
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .semantics {
                    contentDescription = localizationManager.localizedString("close") +
                        ", " + localizationManager.localizedString("close_preview_hint")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(closeButtonSize * 0.4f)
            )
        }

        // Screenshot warning with tap-to-dismiss (matching iOS)
        if (!isPressModeEnabled && showScreenshotWarning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { securityManager.dismissScreenshotWarning() }
            )
            ScreenshotWarningDialog(
                visible = showScreenshotWarning,
                title = localizationManager.localizedString("screenshot_warning_title"),
                message = localizationManager.localizedString("screenshot_warning_message"),
                onDismiss = { securityManager.dismissScreenshotWarning() }
            )
        }

        if (!isPressModeEnabled) {
            RecordingWarningBanner(
                visible = isRecording,
                title = localizationManager.localizedString("screen_recording_detected"),
                message = localizationManager.localizedString("screen_recording_warning_message"),
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
