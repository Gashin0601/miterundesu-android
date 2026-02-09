package com.miterundesu.app.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.LocalizationManager

@Composable
fun ThumbnailView(
    images: List<CapturedImage>,
    onClick: () -> Unit,
    isTheaterMode: Boolean,
    hideContent: Boolean,
    isRecording: Boolean,
    thumbnailSize: Dp,
    modifier: Modifier = Modifier
) {
    val cornerRadius = thumbnailSize * 0.167f
    val iconSize = thumbnailSize * 0.4f
    val blurRadius = thumbnailSize * 0.167f
    val shape = RoundedCornerShape(cornerRadius)
    val latestImage = images.firstOrNull()

    if (latestImage != null) {
        Box(
            modifier = modifier
                .size(thumbnailSize)
                .clip(shape)
                .then(
                    if (!isTheaterMode) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    }
                )
                .alpha(if (isTheaterMode) 0.3f else 1.0f)
                .semantics {
                    contentDescription = LocalizationManager.localizedString("latest_image") +
                        if (isTheaterMode) ", " + LocalizationManager.localizedString("viewing_disabled") else ""
                },
            contentAlignment = Alignment.Center
        ) {
            if (hideContent) {
                // Security: show black rect with white border
                Box(
                    modifier = Modifier
                        .size(thumbnailSize)
                        .clip(shape)
                        .background(Color.Black, shape)
                        .border(2.dp, Color.White, shape)
                )
            } else {
                val bitmap = remember(latestImage.id) {
                    try {
                        BitmapFactory.decodeByteArray(
                            latestImage.imageData,
                            0,
                            latestImage.imageData.size
                        )?.asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                }

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(thumbnailSize)
                            .clip(shape)
                            .border(2.dp, Color.White, shape)
                            .then(
                                if (isRecording) Modifier.blur(blurRadius) else Modifier
                            )
                    )
                } else {
                    PlaceholderIcon(iconSize = iconSize, thumbnailSize = thumbnailSize, cornerRadius = cornerRadius)
                }

                // Time remaining badge (top-right)
                Box(modifier = Modifier.align(Alignment.TopEnd)) {
                    TimeRemainingBadge(
                        image = latestImage
                    )
                }
            }
        }
    } else {
        // Empty state
        Box(
            modifier = modifier
                .size(thumbnailSize)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.2f), shape)
                .semantics {
                    contentDescription = LocalizationManager.localizedString("no_images")
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun PlaceholderIcon(iconSize: Dp, thumbnailSize: Dp, cornerRadius: Dp) {
    Box(
        modifier = Modifier
            .size(thumbnailSize)
            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Photo,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(iconSize)
        )
    }
}
