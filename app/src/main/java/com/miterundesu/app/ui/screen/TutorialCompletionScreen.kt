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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.ui.theme.MainGreen

@Composable
fun TutorialCompletionScreen(
    onboardingManager: OnboardingManager,
    localizationManager: LocalizationManager,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Layered checkmark icon (outer 140dp white 0.2, inner 120dp white, checkmark 60sp MainGreen)
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
            // Checkmark icon
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MainGreen,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(50.dp))

        // Completion title + message merged for TalkBack
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.semantics(mergeDescendants = true) { }
        ) {
            // Completion title
            Text(
                text = localizationManager.localizedString("tutorial_completion_title"),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Completion message
            Text(
                text = localizationManager.localizedString("tutorial_completion_message"),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Start using button
        Button(
            onClick = {
                onboardingManager.completeOnboarding()
                onFinish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(56.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.15f)
                ),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MainGreen
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = localizationManager.localizedString("start_using"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .clearAndSetSemantics { }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
