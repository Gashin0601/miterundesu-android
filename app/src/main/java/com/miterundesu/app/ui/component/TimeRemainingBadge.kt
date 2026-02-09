package com.miterundesu.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.data.model.CapturedImage
import com.miterundesu.app.manager.LocalizationManager
import kotlinx.coroutines.delay

@Composable
fun TimeRemainingBadge(
    image: CapturedImage?,
    modifier: Modifier = Modifier
) {
    if (image == null) return

    var remainingSeconds by remember(image.id) { mutableDoubleStateOf(image.remainingTime) }

    LaunchedEffect(image.id) {
        while (true) {
            delay(1000L)
            remainingSeconds = image.remainingTime
            if (remainingSeconds <= 0.0) break
        }
    }

    val totalSeconds = remainingSeconds.toLong()
    val minutes = (totalSeconds / 60).toInt()
    val seconds = (totalSeconds % 60).toInt()
    val displayText = "$minutes:${"%02d".format(seconds)}"

    // Spoken time for TalkBack (matching iOS spokenRemainingTime)
    val spokenTimeString = LocalizationManager.localizedString(
        "time_remaining_spoken",
        "minutes" to minutes.toString(),
        "seconds" to seconds.toString()
    )

    Text(
        text = displayText,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        fontFamily = FontFamily.Monospace,
        modifier = modifier
            .background(
                Color.Red.copy(alpha = 0.7f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = spokenTimeString }
    )
}
