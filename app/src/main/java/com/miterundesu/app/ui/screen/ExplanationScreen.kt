package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Wheelchair
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.ui.theme.DarkBackground
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange

@Composable
fun ExplanationScreen(
    isTheaterMode: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = if (isTheaterMode) TheaterOrange else MainGreen

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .semantics { contentDescription = "Close" }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\u30DF\u30C6\u30EB\u30F3\u30C7\u30B9",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (isTheaterMode) {
                TheaterModeContent(accentColor = accentColor)
            } else {
                NormalModeContent(accentColor = accentColor)
            }

            Spacer(modifier = Modifier.height(40.dp))

            FooterLinks(
                accentColor = accentColor,
                onWebClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp"))
                    context.startActivity(intent)
                },
                onXClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/miterundesu_jp"))
                    context.startActivity(intent)
                },
                onInstagramClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/miterundesu_jp"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun NormalModeContent(accentColor: Color) {
    Text(
        text = "Privacy-focused camera magnifier for people with visual impairments and elderly users",
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IllustrationIcon(
            icon = Icons.Default.Accessibility,
            label = "Visual\nImpairment",
            color = accentColor
        )
        IllustrationIcon(
            icon = Icons.Default.Elderly,
            label = "Elderly\nUsers",
            color = accentColor
        )
        IllustrationIcon(
            icon = Icons.Default.Wheelchair,
            label = "Wheelchair\nUsers",
            color = accentColor
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    ExplanationSection(
        title = "How to use this app",
        description = "Use the camera to magnify and view items that are difficult to see. " +
                "Pinch to zoom or use the zoom buttons. Take photos to review later " +
                "(photos are automatically deleted after 10 minutes for privacy).",
        accentColor = accentColor
    )

    Spacer(modifier = Modifier.height(20.dp))

    ExplanationSection(
        title = "Why this app is needed",
        description = "Many people with visual impairments or elderly individuals struggle to read " +
                "small text, signs, or exhibits. This app provides a simple, privacy-conscious way " +
                "to magnify and temporarily capture what they need to see.",
        accentColor = accentColor
    )
}

@Composable
private fun TheaterModeContent(accentColor: Color) {
    Text(
        text = "Camera magnifier for use in theaters, museums, and cultural venues",
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 16.sp,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IllustrationIcon(
            icon = Icons.Default.TheaterComedy,
            label = "Theater",
            color = accentColor
        )
        IllustrationIcon(
            icon = Icons.Default.Museum,
            label = "Museum",
            color = accentColor
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    ExplanationSection(
        title = "Using in cultural venues",
        description = "Theater mode dims the UI to minimize disturbance to others. " +
                "The header and footer auto-hide after 15 seconds. " +
                "All captured images are automatically deleted after 10 minutes " +
                "to respect copyright and venue policies.",
        accentColor = accentColor
    )
}

@Composable
private fun IllustrationIcon(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ExplanationSection(
    title: String,
    description: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = accentColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 14.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun FooterLinks(
    accentColor: Color,
    onWebClick: () -> Unit,
    onXClick: () -> Unit,
    onInstagramClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "miterundesu.jp",
            color = accentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onWebClick)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            XLogoButton(
                onClick = onXClick,
                color = Color.White
            )

            InstagramLogoButton(
                onClick = onInstagramClick,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "@miterundesu_jp",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun XLogoButton(
    onClick: () -> Unit,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "X (Twitter)" }
    ) {
        drawXLogo(color)
    }
}

private fun DrawScope.drawXLogo(color: Color) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.1f

    drawLine(
        color = color,
        start = Offset(w * 0.15f, h * 0.15f),
        end = Offset(w * 0.85f, h * 0.85f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(w * 0.85f, h * 0.15f),
        end = Offset(w * 0.15f, h * 0.85f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}

@Composable
private fun InstagramLogoButton(
    onClick: () -> Unit,
    color: Color
) {
    Canvas(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Instagram" }
    ) {
        drawInstagramLogo(color)
    }
}

private fun DrawScope.drawInstagramLogo(color: Color) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.08f
    val cornerRadius = w * 0.25f
    val padding = w * 0.1f

    drawRoundRect(
        color = color,
        topLeft = Offset(padding, padding),
        size = Size(w - padding * 2, h - padding * 2),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = strokeWidth)
    )

    drawCircle(
        color = color,
        radius = w * 0.18f,
        center = Offset(w / 2f, h / 2f),
        style = Stroke(width = strokeWidth)
    )

    drawCircle(
        color = color,
        radius = w * 0.05f,
        center = Offset(w * 0.72f, h * 0.28f),
        style = Fill
    )
}
