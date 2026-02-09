package com.miterundesu.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.provider.Settings
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Bitmap.withWatermark(text: String): Bitmap {
    val result = this.copy(Bitmap.Config.ARGB_8888, true) ?: return this
    val canvas = Canvas(result)

    val imageWidth = result.width.toFloat()

    // Padding: 1.5% of image width (matches iOS)
    val padding = imageWidth * 0.015f

    // Title font: 2% of image width, bold system font (matches iOS)
    val titleFontSize = imageWidth * 0.02f
    val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(102, 255, 255, 255) // 0.4 alpha (matches iOS)
        textSize = titleFontSize
        typeface = Typeface.DEFAULT_BOLD
    }

    // Info font: 1.5% of image width, monospace (matches iOS)
    val infoFontSize = imageWidth * 0.015f
    val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(89, 255, 255, 255) // 0.35 alpha (matches iOS)
        textSize = infoFontSize
        typeface = Typeface.MONOSPACE
    }

    // Measure text heights
    val titleMetrics = titlePaint.fontMetrics
    val titleHeight = titleMetrics.descent - titleMetrics.ascent
    val infoMetrics = infoPaint.fontMetrics
    val infoHeight = infoMetrics.descent - infoMetrics.ascent

    // Total height: title + 2px spacing + info (matches iOS)
    val totalHeight = titleHeight + 2f + infoHeight

    // Position at bottom-left (matches iOS .bottomLeft)
    val x = padding
    val titleY = result.height - totalHeight - padding - titleMetrics.ascent
    val infoY = titleY + titleMetrics.descent + 2f - infoMetrics.ascent

    // Draw text (no shadow, matches iOS bitmap watermark)
    canvas.drawText("ミテルンデス", x, titleY, titlePaint)
    canvas.drawText(text, x, infoY, infoPaint)

    return result
}

fun generateWatermarkText(context: Context): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    val dateStr = LocalDateTime.now().format(formatter)
    val deviceId = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )?.take(6) ?: "000000"
    return "$dateStr | ID: $deviceId"
}
