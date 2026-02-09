package com.miterundesu.app.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.platform.LocalView
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
import java.util.UUID

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
    val maxZoomFactor by settingsManager.maxZoomFactor.collectAsStateWithLifecycle()
    var showDeletedScreen by remember { mutableStateOf(false) }
    var shouldDismissAfterDeletedView by remember { mutableStateOf(false) }
    var wasInBackground by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val view = LocalView.current

    // Per-image zoom state maps (matching iOS imageScales/imageOffsets)
    val imageScales = remember { mutableStateMapOf<UUID, Float>() }
    val imageOffsets = remember { mutableStateMapOf<UUID, Offset>() }
    var isZoomed by remember { mutableStateOf(false) }

    // Close if no images
    LaunchedEffect(images) {
        if (images.isEmpty()) {
            onClose()
        }
    }

    // TalkBack: Announce gallery opened (matching iOS announceGalleryOpened)
    LaunchedEffect(Unit) {
        delay(500L) // Match iOS 0.5s delay
        view.announceForAccessibility(
            localizationManager.localizedString("photo_gallery")
        )
    }

    // TalkBack: Announce page changes (matching iOS announcePhotoChange)
    var previousPage by remember { mutableStateOf(0) }

    // Per-second cleanup (matching iOS timer behavior)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (showDeletedScreen) continue
            imageManager.removeExpiredImages()
        }
    }

    // Check for expired images on resume and handle background-aware lifecycle
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    wasInBackground = true
                }
                Lifecycle.Event.ON_RESUME -> {
                    imageManager.removeExpiredImages()
                    if (wasInBackground && images.isEmpty()) {
                        onClose()
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

    if (images.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { images.size })

    // TalkBack: Announce page changes (matching iOS announcePhotoChange)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != previousPage) {
            val message = localizationManager.localizedString(
                "moved_to_photo",
                "number" to (pagerState.currentPage + 1).toString(),
                "total" to images.size.toString()
            )
            view.announceForAccessibility(message)
            previousPage = pagerState.currentPage
        }
    }

    val currentImage by remember(pagerState.currentPage, images) {
        derivedStateOf {
            images.getOrNull(pagerState.currentPage)
        }
    }

    // Current page zoom state (derived from maps, matching iOS currentScale/currentOffset)
    val currentScale by remember {
        derivedStateOf {
            val img = images.getOrNull(pagerState.currentPage)
            if (img != null) imageScales[img.id] ?: 1f else 1f
        }
    }
    val currentOffset by remember {
        derivedStateOf {
            val img = images.getOrNull(pagerState.currentPage)
            if (img != null) imageOffsets[img.id] ?: Offset.Zero else Offset.Zero
        }
    }

    // Monitor current image expiry with adjust-to-next behavior (matching iOS)
    LaunchedEffect(currentImage?.id, images.size) {
        val img = currentImage ?: return@LaunchedEffect
        while (true) {
            delay(1000L)
            if (img.isExpired) {
                if (images.size <= 1) {
                    if (wasInBackground) {
                        onClose()
                    } else {
                        shouldDismissAfterDeletedView = true
                        showDeletedScreen = true
                    }
                } else {
                    if (wasInBackground) {
                        // Silently adjust to next image
                    } else {
                        shouldDismissAfterDeletedView = false
                        showDeletedScreen = true
                    }
                }
                break
            }
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
                imageManager.removeExpiredImages()
                if (shouldDismissAfterDeletedView || images.isEmpty()) {
                    onClose()
                }
            }
        )
        return
    }

    // Responsive sizes (matching iOS)
    val horizontalPadding = screenWidth * 0.05f
    val verticalPadding = screenWidth * 0.01f
    val buttonSize = (screenWidth * 0.11f).dp
    val indicatorSize = (screenWidth * 0.02f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen)  // Background: MainGreen (not black, matching iOS)
    ) {
        // hideContent: full black overlay (matching iOS securityManager.hideContent)
        if (shouldShowBlackOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        } else {
            // Image pager
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isZoomed,
                modifier = Modifier
                    .fillMaxSize()
                    .semantics {
                        val currentPage = pagerState.currentPage + 1
                        val totalPages = images.size
                        contentDescription =
                            localizationManager.localizedString("photo_gallery") +
                                    ", " + localizationManager.localizedString("photo_count")
                                .replace("{count}", totalPages.toString())
                    }
            ) { page ->
                val image = images.getOrNull(page)
                if (image != null) {
                    val pageScale = imageScales[image.id] ?: 1f
                    val pageOffset = imageOffsets[image.id] ?: Offset.Zero

                    Box(modifier = Modifier.fillMaxSize()) {
                        ZoomableImage(
                            scale = pageScale,
                            offset = pageOffset,
                            onScaleChange = { newScale ->
                                imageScales[image.id] = newScale
                                if (page == pagerState.currentPage) {
                                    isZoomed = newScale > 1.01f
                                }
                            },
                            onOffsetChange = { newOffset ->
                                imageOffsets[image.id] = newOffset
                            },
                            maxScale = maxZoomFactor,
                            modifier = if (shouldBlur) {
                                Modifier
                                    .fillMaxSize()
                                    .blur(50.dp) // blur radius 50 (matching iOS)
                            } else {
                                Modifier.fillMaxSize()
                            },
                            onZoomChanged = { zoomed ->
                                if (page == pagerState.currentPage) {
                                    isZoomed = zoomed
                                }
                            }
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
        }

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

        // Top controls (matching iOS: time LEFT, explanation CENTER, close RIGHT)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = horizontalPadding.dp, end = horizontalPadding.dp)
                .align(Alignment.TopCenter)
        ) {
            // Left: remaining time badge (matching iOS position)
            TimeRemainingBadge(
                image = currentImage,
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

            // Right: Close button (matching iOS pill style)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .clickable(onClick = onClose)
                    .padding(horizontal = (horizontalPadding * 0.6f).dp, vertical = (verticalPadding * 0.6f).dp)
                    .semantics { contentDescription = localizationManager.localizedString("close") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = localizationManager.localizedString("close"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Page indicator dots (responsive sizing: screenWidth * 0.02, matching iOS)
        if (images.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .clearAndSetSemantics { }, // Decorative (matching iOS accessibilityHidden)
                horizontalArrangement = Arrangement.spacedBy(indicatorSize * 0.4f)
            ) {
                repeat(images.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(indicatorSize)
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

        // Zoom controls + zoom level display: vertical column on bottom-RIGHT (matching iOS)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = (horizontalPadding * 0.6f).dp, bottom = (screenWidth * 0.06f).dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy((screenWidth * 0.11f * 0.18f).dp)
        ) {
            // ZoomControls component wired to shared state (matching iOS)
            ZoomControls(
                currentZoom = currentScale,
                currentOffset = currentOffset,
                maxZoom = maxZoomFactor,
                onZoomChange = { newScale ->
                    val img = currentImage ?: return@ZoomControls
                    imageScales[img.id] = newScale
                    isZoomed = newScale > 1.01f
                },
                onOffsetChange = { newOffset ->
                    val img = currentImage ?: return@ZoomControls
                    imageOffsets[img.id] = newOffset
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
