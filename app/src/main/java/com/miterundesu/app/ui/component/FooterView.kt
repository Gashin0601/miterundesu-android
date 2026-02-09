package com.miterundesu.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miterundesu.app.ui.screen.LocalSpotlightFrames
import com.miterundesu.app.data.model.CapturedImage

@Composable
fun FooterView(
    images: List<CapturedImage>,
    zoomFactor: Float,
    isCapturing: Boolean,
    isTheaterMode: Boolean,
    hideContent: Boolean,
    isRecording: Boolean,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    screenWidth: Dp,
    modifier: Modifier = Modifier,
    shutterButton: @Composable (Dp) -> Unit
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val horizontalPadding = screenWidth * 0.051f
    val shutterSize = screenWidth * 0.22f
    val thumbnailSize = screenWidth * 0.18f
    val topPadding = screenHeightDp * 0.009f
    val bottomPadding = screenHeightDp * 0.023f

    val spotlightFrames = LocalSpotlightFrames.current

    // Box layout: shutter centered, thumbnail and zoom overlaid (matching iOS ZStack pattern)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = topPadding,
                bottom = bottomPadding
            ),
        contentAlignment = Alignment.Center
    ) {
        // Thumbnail on the left
        ThumbnailView(
            images = images,
            onClick = onGalleryClick,
            isTheaterMode = isTheaterMode,
            hideContent = hideContent,
            isRecording = isRecording,
            thumbnailSize = thumbnailSize,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .onGloballyPositioned {
                    spotlightFrames["photo_button"] = it.boundsInWindow()
                }
        )

        // Shutter button centered
        Box(
            modifier = Modifier.onGloballyPositioned {
                spotlightFrames["shutter_button"] = it.boundsInWindow()
            },
            contentAlignment = Alignment.Center
        ) {
            shutterButton(shutterSize)
        }

        // Zoom level on the right
        ZoomLevelView(
            zoomFactor = zoomFactor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .onGloballyPositioned {
                    spotlightFrames["zoom_controls"] = it.boundsInWindow()
                }
        )
    }
}
