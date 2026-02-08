package com.miterundesu.app.ui.component

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.R
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun WatermarkView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var watermarkText by remember { mutableStateOf("") }

    val deviceId = remember {
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.take(6) ?: "000000"
    }

    fun updateText() {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        val dateStr = LocalDateTime.now().format(formatter)
        watermarkText = "$dateStr | ID: $deviceId"
    }

    LaunchedEffect(Unit) {
        updateText()
        while (true) {
            delay(60_000L)
            updateText()
        }
    }

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Logo image
        try {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier
                    .height(10.dp)
                    .alpha(0.4f),
                contentScale = ContentScale.FillHeight
            )
        } catch (_: Exception) {
            // Logo resource not available yet
        }

        // Info text
        Text(
            text = watermarkText,
            color = Color.White,
            fontSize = 8.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.alpha(0.35f)
        )
    }
}
