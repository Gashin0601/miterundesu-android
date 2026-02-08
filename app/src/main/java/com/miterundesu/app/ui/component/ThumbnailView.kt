package com.miterundesu.app.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.data.model.CapturedImage

@Composable
fun ThumbnailView(
    images: List<CapturedImage>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .border(2.dp, Color.White, shape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Captured photos gallery" },
        contentAlignment = Alignment.Center
    ) {
        val latestImage = images.lastOrNull()

        if (latestImage != null) {
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
                        .size(48.dp)
                        .clip(shape)
                )
            } else {
                PlaceholderIcon()
            }
        } else {
            PlaceholderIcon()
        }

        if (images.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(18.dp)
                    .background(Color.Red, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${images.size}",
                    color = Color.White,
                    fontSize = 10.sp,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@Composable
private fun PlaceholderIcon() {
    Icon(
        imageVector = Icons.Default.CameraAlt,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.6f),
        modifier = Modifier.size(24.dp)
    )
}
