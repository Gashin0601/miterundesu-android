package com.miterundesu.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miterundesu.app.manager.LocalizationManager

@Composable
fun ShutterButton(
    isCapturing: Boolean,
    isTheaterMode: Boolean,
    onClick: () -> Unit,
    buttonSize: Dp,
    modifier: Modifier = Modifier
) {
    val isDisabled = isTheaterMode || isCapturing
    val outerStroke = buttonSize * 0.057f
    val innerSize = buttonSize * 0.857f

    val accessibilityLabel = LocalizationManager.localizedString(
        when {
            isTheaterMode -> "capture_disabled"
            isCapturing -> "capturing"
            else -> "capture"
        }
    )

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(CircleShape)
            .border(outerStroke, Color.White, CircleShape)
            .then(
                if (!isDisabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .alpha(if (isDisabled) 0.3f else 1.0f)
            .semantics {
                contentDescription = accessibilityLabel
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(CircleShape)
                .background(
                    if (isDisabled) Color.Gray else Color.White,
                    CircleShape
                )
        )
    }
}
