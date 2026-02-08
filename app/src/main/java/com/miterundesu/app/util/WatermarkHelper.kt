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

    val textSize = result.height * 0.018f
    val titleTextSize = textSize * 1.3f
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(90, 255, 255, 255) // semi-transparent white
        this.textSize = textSize
        typeface = Typeface.MONOSPACE
        setShadowLayer(2f, 1f, 1f, android.graphics.Color.argb(128, 0, 0, 0))
    }

    val x = textSize * 0.8f
    val y = result.height - textSize * 0.8f

    // Draw app title line above the info text
    val titlePaint = Paint(paint).apply {
        this.textSize = titleTextSize
    }
    val titleY = y - textSize * 1.2f
    canvas.drawText("ミテルンデス", x, titleY, titlePaint)

    canvas.drawText(text, x, y, paint)

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
