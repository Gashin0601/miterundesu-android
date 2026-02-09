package com.miterundesu.app.ui.component

import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.R
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WatermarkView(
    modifier: Modifier = Modifier,
    isDarkBackground: Boolean = true
) {
    val context = LocalContext.current
    var watermarkText by remember { mutableStateOf("") }

    val deviceId = remember {
        Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.take(6)?.uppercase(Locale.ROOT) ?: "000000"
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
        modifier = modifier.clearAndSetSemantics { },
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Logo image (matching iOS Image("Logo") - wide format with text)
        Image(
            painter = painterResource(id = R.drawable.logo_wide),
            contentDescription = null,
            modifier = Modifier
                .height(10.dp)
                .alpha(0.4f)
                .clearAndSetSemantics { },
            contentScale = ContentScale.FillHeight
        )

        // Info text
        Text(
            text = watermarkText,
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.6f),
                    offset = Offset(0f, 0.5f),
                    blurRadius = 1f
                )
            ),
            modifier = Modifier.clearAndSetSemantics { }
        )
    }
}
