package com.miterundesu.app.ui.screen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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

    // Auto-dismiss after 2.5s when TalkBack is not enabled (matching iOS)
    if (!isTalkBackEnabled) {
        LaunchedEffect(Unit) {
            delay(2500L)
            onDismiss()
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

            // Timer icon (matching iOS: 120pt light weight)
            Icon(
                imageVector = Icons.Filled.Timer,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(120.dp)
                    .clearAndSetSemantics { }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Title + subtitle merged for TalkBack
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.semantics(mergeDescendants = true) { }
            ) {
                // Title: fontSize = 36.sp, fontWeight = Bold (matching iOS .system(size: 36, weight: .bold, design: .rounded))
                Text(
                    text = localizationManager.localizedString("image_deleted_title"),
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subtitle: fontSize = 18.sp, fontWeight = Medium, color white 0.9 opacity, lineSpacing 6
                // Horizontal padding 40.dp (matching iOS)
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

            // Close button (VoiceOver/TalkBack only, matching iOS):
            // full-width with text + xmark icon, MainGreen text on white background,
            // shadow, RoundedCornerShape(16.dp), verticalPadding 18.dp
            // Horizontal padding 32.dp, bottom padding 40.dp
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
                        .clickable(onClick = onDismiss)
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
