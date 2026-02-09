package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Museum
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.miterundesu.app.R
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.component.TheaterModeToggle
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange

@Composable
fun ExplanationScreen(
    localizationManager: LocalizationManager,
    isTheaterMode: Boolean,
    onToggleTheaterMode: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // Background: MainGreen for normal mode, TheaterOrange for theater mode (matching iOS)
    val backgroundColor = if (isTheaterMode) TheaterOrange else MainGreen

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Top header matching iOS: theater toggle (left), logo (center), close button (right)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            // Left: TheaterModeToggle
            TheaterModeToggle(
                isTheaterMode = isTheaterMode,
                onToggle = onToggleTheaterMode,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // Center: Logo image (matching iOS Image("Logo") - wide format with text)
            Image(
                painter = painterResource(id = R.drawable.logo_wide),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .heightIn(max = 20.dp),
                contentScale = ContentScale.Fit
            )

            // Right: Close button - pill shape with xmark + "close" text (matching iOS)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        Color.White.copy(alpha = 0.25f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable(onClick = onClose)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .semantics { contentDescription = localizationManager.localizedString("close") },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clearAndSetSemantics { }
                )
                Text(
                    text = localizationManager.localizedString("close"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Title (matching iOS Japanese text)
            Text(
                text = "\u64AE\u5F71\u3057\u3066\u3044\u308B\u308F\u3051\u3067\u306F\u306A\u304F\u3001\n\u62E1\u5927\u3057\u3066\u898B\u3066\u3044\u308B\u3093\u3067\u3059\u3002",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Body text (matching iOS, mode-dependent)
            Text(
                text = if (isTheaterMode) {
                    "\u30DF\u30C6\u30EB\u30F3\u30C7\u30B9\u306F\u3001\u64AE\u5F71\u3084\u9332\u753B\u3092\u76EE\u7684\u3068\u305B\u305A\u3001\"\u898B\u308B\u305F\u3081\u3060\u3051\"\u306B\u4F7F\u3048\u308B\u30AB\u30E1\u30E9\u30A2\u30D7\u30EA\u3067\u3059\u3002\n\u6620\u753B\u9928\u30FB\u7F8E\u8853\u9928\u30FB\u535A\u7269\u9928\u306A\u3069\u3001\u64AE\u5F71\u304C\u7981\u6B62\u3055\u308C\u3066\u3044\u308B\u5834\u6240\u3067\u3082\u3001\u5B89\u5FC3\u3057\u3066\"\u62E1\u5927\u3057\u3066\u898B\u308B\"\u3053\u3068\u304C\u3067\u304D\u307E\u3059\u3002\n\n\u30A2\u30D7\u30EA\u3067\u306F\u5199\u771F\u30FB\u52D5\u753B\u306E\u64AE\u5F71\u306F\u5B8C\u5168\u306B\u4E0D\u53EF\u3002\n\u3055\u3089\u306B\u3001\u30B9\u30AF\u30EA\u30FC\u30F3\u30B7\u30E7\u30C3\u30C8\u3084\u753B\u9762\u53CE\u9332\u3082\u7121\u52B9\u5316\u3055\u308C\u3066\u304A\u308A\u3001\u5B89\u5FC3\u3057\u3066\u3054\u5229\u7528\u3044\u305F\u3060\u3051\u307E\u3059\u3002"
                } else {
                    "\u30DF\u30C6\u30EB\u30F3\u30C7\u30B9\u306F\u3001\u64AE\u5F71\u3084\u9332\u753B\u3067\u306F\u306A\u304F\u3001\u62E1\u5927\u93E1\u3068\u3057\u3066\u30B9\u30DE\u30FC\u30C8\u30D5\u30A9\u30F3\u3092\u4F7F\u3046\u305F\u3081\u306E\u30A2\u30D7\u30EA\u3067\u3059\u3002\n\u5F31\u8996\u3084\u8001\u773C\u306A\u3069\u3001\u898B\u3048\u3065\u3089\u3055\u3092\u611F\u3058\u308B\u65B9\u304C\u5B89\u5FC3\u3057\u3066\u65E5\u5E38\u306E\u4E2D\u3067\u300C\u898B\u308B\u300D\u3053\u3068\u3092\u30B5\u30DD\u30FC\u30C8\u3057\u307E\u3059\u3002\n\u64AE\u5F71\u3057\u305F\u753B\u50CF\u306F10\u5206\u5F8C\u306B\u81EA\u52D5\u3067\u524A\u9664\u3055\u308C\u3001\u30B9\u30AF\u30EA\u30FC\u30F3\u30B7\u30E7\u30C3\u30C8\u3084\u753B\u9762\u53CE\u9332\u3082\u3067\u304D\u307E\u305B\u3093\u3002"
                },
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isTheaterMode) {
                TheaterModeContent()
            } else {
                NormalModeContent()
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer section (matching iOS)
            FooterLinks(
                onWebClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp"))
                    context.startActivity(intent)
                },
                onXClick = {
                    // Matching iOS URL with query param
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/miterundesu_jp?s=11"))
                    context.startActivity(intent)
                },
                onInstagramClick = {
                    // Matching iOS URL with query param
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/miterundesu_jp/?utm_source=ig_web_button_share_sheet"))
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun NormalModeContent() {
    // 3 icons in a row at 80dp (matching iOS spacing)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally)
    ) {
        Icon(
            imageVector = Icons.Default.Accessibility,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
        Icon(
            imageVector = Icons.Default.Elderly,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
        Icon(
            imageVector = Icons.Default.Accessible,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(80.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Explanation items matching iOS Japanese text
    ExplanationItem(
        subtitle = "\u898B\u3048\u3065\u3089\u3044\u65B9\uFF08\u5F31\u8996\u30FB\u8001\u773C\uFF09",
        description = "\u30B3\u30F3\u30D3\u30CB\u3084\u30B9\u30FC\u30D1\u30FC\u3067\u306F\u3001\u5E97\u5185\u64AE\u5F71\u3092\u7981\u6B62\u3059\u308B\u8CBC\u308A\u7D19\u304C\u5897\u3048\u3066\u304D\u3066\u3044\u307E\u3059\u3002\n\u3057\u304B\u3057\u3001\u5546\u54C1\u3092\u3057\u3063\u304B\u308A\u78BA\u8A8D\u3059\u308B\u305F\u3081\u306B\u306F\u3001\u30B9\u30DE\u30FC\u30C8\u30D5\u30A9\u30F3\u3067\"\u62E1\u5927\u3057\u3066\u898B\u308B\"\u3053\u3068\u304C\u5FC5\u8981\u306A\u5834\u9762\u304C\u3042\u308A\u307E\u3059\u3002"
    )

    Spacer(modifier = Modifier.height(24.dp))

    ExplanationItem(
        subtitle = "\u8ECA\u6905\u5B50\u30E6\u30FC\u30B6\u30FC",
        description = "\u68DA\u304C\u9AD8\u304F\u3001\u5546\u54C1\u304C\u76EE\u306E\u9AD8\u3055\u306B\u5165\u3089\u306A\u3044\u3053\u3068\u304C\u3042\u308A\u307E\u3059\u3002\n\u305D\u306E\u305F\u3081\u3001\u624B\u3092\u4F38\u3070\u3057\u3066\u5199\u771F\u3092\u64AE\u308A\u3001\u62E1\u5927\u3057\u3066\u78BA\u8A8D\u3059\u308B\u5FC5\u8981\u304C\u3042\u308B\u306E\u3067\u3059\u3002"
    )
}

@Composable
private fun TheaterModeContent() {
    // 2 icons in a row at 100dp (matching iOS spacing)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally)
    ) {
        Icon(
            imageVector = Icons.Default.TheaterComedy,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )
        Icon(
            imageVector = Icons.Default.Museum,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(100.dp)
        )
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Explanation items matching iOS Japanese text - TWO sections
    ExplanationItem(
        subtitle = "\u6620\u753B\u9928",
        description = "\u5B57\u5E55\u3084\u8868\u60C5\u304C\u898B\u3048\u3065\u3089\u3044\u3068\u304D\u3001\u30B9\u30DE\u30DB\u306E\u30AB\u30E1\u30E9\u3067\u5FC5\u8981\u306A\u90E8\u5206\u3060\u3051\u5C11\u3057\u62E1\u5927\u3057\u3066\u9451\u8CDC\u3067\u304D\u307E\u3059\u3002\n\u753B\u9762\u306E\u5149\u306F\u6700\u5C0F\u9650\u306B\u6291\u3048\u3089\u308C\u308B\u305F\u3081\u3001\u5468\u56F2\u306E\u8FF7\u60D1\u306B\u306A\u3089\u305A\u6620\u753B\u3092\u697D\u3057\u3081\u307E\u3059\u3002"
    )

    Spacer(modifier = Modifier.height(24.dp))

    ExplanationItem(
        subtitle = "\u7F8E\u8853\u9928\u30FB\u535A\u7269\u9928",
        description = "\u5C55\u793A\u7269\u306E\u305D\u3070\u306B\u3042\u308B\u7D30\u304B\u306A\u6587\u5B57\u3084\u8AAC\u660E\u30D7\u30EC\u30FC\u30C8\u3092\u62E1\u5927\u3057\u3066\u8AAD\u307F\u3084\u3059\u304F\u3067\u304D\u307E\u3059\u3002\n\u7167\u660E\u304C\u6697\u3044\u5C55\u793A\u5BA4\u3067\u3082\u3001\u62E1\u5927\u8868\u793A\u306B\u3088\u3063\u3066\u6587\u5B57\u3092\u306F\u3063\u304D\u308A\u78BA\u8A8D\u3067\u304D\u307E\u3059\u3002"
    )
}

@Composable
private fun ExplanationItem(
    subtitle: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = subtitle,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun FooterLinks(
    onWebClick: () -> Unit,
    onXClick: () -> Unit,
    onInstagramClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Official site link with link icon (matching iOS: link.circle.fill + text)
        Row(
            modifier = Modifier
                .clickable(onClick = onWebClick)
                .semantics {
                    contentDescription = "miterundesu.jp"
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .clearAndSetSemantics { }
            )
            Text(
                text = "miterundesu.jp",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SNS links (matching iOS spacing)
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
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

        // Copyright (matching iOS: "(c) 2025 Miterundesu")
        Text(
            text = "\u00A9 2025 Miterundesu",
            color = Color.White.copy(alpha = 0.6f),
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
            .size(50.dp)
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
            .size(50.dp)
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Instagram" }
    ) {
        drawInstagramLogo(color)
    }
}

private fun DrawScope.drawInstagramLogo(color: Color) {
    val w = size.width
    val h = size.height
    val strokeWidth = w * 0.06f
    val cornerRadius = w * 0.24f
    val padding = w * 0.08f

    drawRoundRect(
        color = color,
        topLeft = Offset(padding, padding),
        size = Size(w - padding * 2, h - padding * 2),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = strokeWidth)
    )

    drawCircle(
        color = color,
        radius = w * 0.24f,
        center = Offset(w / 2f, h / 2f),
        style = Stroke(width = strokeWidth)
    )

    drawCircle(
        color = color,
        radius = w * 0.04f,
        center = Offset(w * 0.74f, h * 0.26f),
        style = Fill
    )
}
