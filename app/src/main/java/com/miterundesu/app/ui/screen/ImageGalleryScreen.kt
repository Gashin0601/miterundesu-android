package com.miterundesu.app.ui.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import kotlinx.coroutines.delay

@Composable
fun ImageGalleryScreen(
    imageManager: ImageManager,
    securityManager: SecurityManager,
    settingsManager: SettingsManager,
    pressModeManager: PressModeManager,
    localizationManager: LocalizationManager,
    onClose: () -> Unit,
    onExplanation: () -> Unit,
    onImageDeleted: () -> Unit
) {
    val images by imageManager.images.collectAsStateWithLifecycle()
    val isRecording by securityManager.isRecording.collectAsStateWithLifecycle()
    val hideContent by securityManager.hideContent.collectAsStateWithLifecycle()
    val showScreenshotWarning by securityManager.showScreenshotWarning.collectAsStateWithLifecycle()
    val isPressModeEnabled by pressModeManager.isPressModeEnabled.collectAsStateWithLifecycle()
    val isTheaterMode by settingsManager.isTheaterMode.collectAsStateWithLifecycle()
    var showDeletedScreen by remember { mutableStateOf(false) }
    var currentZoom by remember { mutableFloatStateOf(1f) }
    var isZoomed by remember { mutableStateOf(false) }

    // Close if no images
    LaunchedEffect(images) {
        if (images.isEmpty()) {
            onClose()
        }
    }

    // Check for expired images on resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                imageManager.removeExpiredImages()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })

    val currentImage by remember(pagerState.currentPage, images) {
        derivedStateOf {
            images.getOrNull(pagerState.currentPage)
        }
    }

    // Monitor current image expiry
    LaunchedEffect(currentImage?.id) {
        val img = currentImage ?: return@LaunchedEffect
        while (true) {
            delay(1000L)
            if (img.isExpired) {
                showDeletedScreen = true
                break
            }
        }
    }

    val shouldBlur = !isPressModeEnabled && (hideContent || isRecording)

    if (showDeletedScreen) {
        ImageDeletedScreen(
            localizationManager = localizationManager,
            onDismiss = {
                showDeletedScreen = false
                imageManager.removeExpiredImages()
                if (images.isEmpty()) {
                    onClose()
                }
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Image pager
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = !isZoomed,
            modifier = Modifier
                .fillMaxSize()
                .semantics {
                    val currentPage = pagerState.currentPage + 1
                    val totalPages = images.size
                    contentDescription = "${totalPages}枚中${currentPage}枚目"
                }
        ) { page ->
            val image = images.getOrNull(page)
            if (image != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ZoomableImage(
                        modifier = if (shouldBlur) {
                            Modifier.fillMaxSize().blur(30.dp)
                        } else {
                            Modifier.fillMaxSize()
                        },
                        onZoomChanged = { zoomed -> isZoomed = zoomed }
                    ) {
                        val bitmap = remember(image.id) {
                            BitmapFactory.decodeByteArray(
                                image.imageData, 0, image.imageData.size
                            )
                        }
                        if (bitmap != null && !bitmap.isRecycled) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = localizationManager.localizedString("accessibility_image_viewer"),
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // Watermark overlay
                    WatermarkView(
                        modifier = Modifier.align(Alignment.BottomStart)
                    )
                }
            }
        }

        // Zoom controls overlay
        ZoomControls(
            currentZoom = currentZoom,
            maxZoom = 10f,
            onZoomChange = { currentZoom = it },
            onSmoothZoom = { target, _ -> currentZoom = target },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        // Zoom level display
        AnimatedVisibility(
            visible = isZoomed,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 100.dp, start = 16.dp)
        ) {
            Text(
                text = "${String.format("%.1f", currentZoom)}x",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // Top controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopCenter)
        ) {
            // Close button (left)
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = localizationManager.localizedString("close"),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Time remaining badge (center)
            TimeRemainingBadge(
                image = currentImage,
                modifier = Modifier.align(Alignment.Center)
            )

            // Explanation button (right, normal mode only)
            if (!isTheaterMode) {
                IconButton(
                    onClick = onExplanation,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = localizationManager.localizedString("accessibility_explanation_button"),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Page indicator dots
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.4f)
                                }
                            )
                    )
                }
            }
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
