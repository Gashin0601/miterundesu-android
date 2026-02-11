package com.miterundesu.app.ui.screen

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.theme.MainGreen
import kotlinx.coroutines.delay

@Composable
fun ImageDeletedScreen(
    localizationManager: LocalizationManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accessibilityManager = remember {
        context.getSystemService(AccessibilityManager::class.java)
    }
    val isTalkBackEnabled = remember {
        accessibilityManager?.isTouchExplorationEnabled == true
    }

    // onDismissの二重呼び出し防止
    var hasClosed by remember { mutableStateOf(false) }
    val closeSafely: () -> Unit = remember(onDismiss) {
        {
            if (!hasClosed) {
                hasClosed = true
                onDismiss()
            }
        }
    }

    // 触覚フィードバック（警告）
    LaunchedEffect(Unit) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
        vibrator?.vibrate(
            VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE)
        )
    }

    // TalkBackアナウンス + 自動消去
    LaunchedEffect(Unit) {
        if (isTalkBackEnabled) {
            // TalkBackアナウンス
            val event = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            } else {
                @Suppress("DEPRECATION")
                AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT)
            }
            event.text.add(localizationManager.localizedString("image_deleted_title"))
            accessibilityManager?.sendAccessibilityEvent(event)

            // 読み上げ完了後に自動消去（3秒）
            delay(3000L)
            closeSafely()
        } else {
            // TalkBack OFF: 2.5秒後に自動消去
            delay(2500L)
            closeSafely()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // ゴミ箱アイコン（TutorialCompletionScreenと同じサークルパターン）
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clearAndSetSemantics { }
            ) {
                // Outer circle
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )
                // Inner circle
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                )
                // Trash icon
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = MainGreen,
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // メッセージ（TalkBack用にマージ）
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) { }
            ) {
                // Title: 32sp bold (matching iOS .system(size: 32, weight: .bold, design: .rounded))
                Text(
                    text = localizationManager.localizedString("image_deleted_title"),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Subtitle: 18sp medium, white 0.9 opacity
                Text(
                    text = localizationManager.localizedString("image_deleted_reason"),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 閉じるボタン（TalkBack有効時のみ表示）
            if (isTalkBackEnabled) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 40.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .clickable(onClick = closeSafely)
                        .padding(vertical = 18.dp)
                        .semantics(mergeDescendants = true) {
                            contentDescription = localizationManager.localizedString("close")
                        },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizationManager.localizedString("close"),
                        color = MainGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MainGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
