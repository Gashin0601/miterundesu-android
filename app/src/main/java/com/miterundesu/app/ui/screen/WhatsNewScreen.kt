package com.miterundesu.app.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.WhatsNewManager
import com.miterundesu.app.ui.theme.MainGreen

@Composable
fun WhatsNewScreen(
    whatsNewManager: WhatsNewManager,
    localizationManager: LocalizationManager,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen)
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Spacer(modifier = Modifier.weight(1f))

        // Title + version merged for TalkBack
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(mergeDescendants = true) { }
        ) {
            // Title
            Text(
                text = localizationManager.localizedString("whats_new_title"),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Version label
            Text(
                text = "v1.1.0",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Feature list
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Feature 1: 1x button long press
            FeatureRow(
                number = "1",
                title = localizationManager.localizedString("whats_new_feature1_title"),
                description = localizationManager.localizedString("whats_new_feature1_desc")
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Close button (white background, MainGreen text - matching iOS)
        Button(
            onClick = {
                whatsNewManager.markWhatsNewAsSeen()
                onDismiss()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MainGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = localizationManager.localizedString("whats_new_close"),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
private fun FeatureRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, $description"
            },
        verticalAlignment = Alignment.Top
    ) {
        // Number circle icon (equivalent to iOS "1.circle.fill")
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color.White,
                    shape = CircleShape
                )
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = MainGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
