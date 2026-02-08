package com.miterundesu.app.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.miterundesu.app.data.model.CapturedImage

@Composable
fun FooterView(
    images: List<CapturedImage>,
    zoomFactor: Float,
    isCapturing: Boolean,
    onShutterClick: () -> Unit,
    onGalleryClick: () -> Unit,
    modifier: Modifier = Modifier,
    shutterButton: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val latestImage = images.lastOrNull()
            if (latestImage != null) {
                TimeRemainingBadge(
                    image = latestImage,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            ThumbnailView(
                images = images,
                onClick = onGalleryClick
            )
        }

        Box(contentAlignment = Alignment.Center) {
            shutterButton()
        }

        ZoomLevelView(
            zoomFactor = zoomFactor
        )
    }
}
