package com.miterundesu.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.R
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.ui.theme.MainGreen

@Composable
fun TutorialWelcomeScreen(
    onboardingManager: OnboardingManager,
    localizationManager: LocalizationManager,
    onStartTutorial: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainGreen)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Logo
        try {
            Image(
                painter = painterResource(id = R.drawable.logo_square),
                contentDescription = "ミテルンデス",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )
        } catch (_: Exception) {
            // Logo resource not yet available
            Spacer(modifier = Modifier.size(200.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Welcome title
        Text(
            text = localizationManager.localizedString("tutorial_welcome_title"),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Welcome message
        Text(
            text = localizationManager.localizedString("tutorial_welcome_message"),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // Get Started button
        Button(
            onClick = {
                onboardingManager.completeWelcomeScreen()
                onStartTutorial()
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
                text = localizationManager.localizedString("tutorial_start"),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Skip button
        TextButton(
            onClick = {
                onboardingManager.completeOnboarding()
                onSkip()
            }
        ) {
            Text(
                text = localizationManager.localizedString("tutorial_skip"),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
