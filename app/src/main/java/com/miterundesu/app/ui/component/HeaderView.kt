package com.miterundesu.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.ui.screen.LocalSpotlightFrames
import com.miterundesu.app.R
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange

@Composable
fun HeaderView(
    isTheaterMode: Boolean,
    scrollingMessage: String,
    onTheaterToggle: () -> Unit,
    onExplanationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    controlsVisible: Boolean = true
) {
    val accentColor = if (isTheaterMode) TheaterOrange else MainGreen
    val spotlightFrames = LocalSpotlightFrames.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TheaterModeToggle(
                isTheaterMode = isTheaterMode,
                onToggle = onTheaterToggle,
                modifier = Modifier.onGloballyPositioned {
                    spotlightFrames["theater_toggle"] = it.boundsInWindow()
                }
            )

            // "説明を見る" button: white pill with accent text + book icon (matching iOS)
            Row(
                modifier = Modifier
                    .onGloballyPositioned {
                        spotlightFrames["explanation_button"] = it.boundsInWindow()
                    }
                    .then(
                        if (controlsVisible) {
                            Modifier
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .clickable(onClick = onExplanationClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .semantics {
                                    contentDescription = LocalizationManager.localizedString("explanation")
                                }
                        } else {
                            Modifier
                                .background(Color.White, RoundedCornerShape(20.dp))
                                .clickable(onClick = onExplanationClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clearAndSetSemantics { }
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = LocalizationManager.localizedString("explanation"),
                    color = accentColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // "設定" button: semi-transparent white pill (matching iOS)
            Row(
                modifier = Modifier
                    .onGloballyPositioned {
                        spotlightFrames["settings_button"] = it.boundsInWindow()
                    }
                    .then(
                        if (controlsVisible) {
                            Modifier
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(onClick = onSettingsClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .semantics {
                                    contentDescription = LocalizationManager.localizedString("settings")
                                }
                        } else {
                            Modifier
                                .background(
                                    Color.White.copy(alpha = 0.25f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(onClick = onSettingsClick)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clearAndSetSemantics { }
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = LocalizationManager.localizedString("settings"),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Logo image (matching iOS Image("Logo") - wide format with text)
        Image(
            painter = painterResource(id = R.drawable.logo_wide),
            contentDescription = null,
            modifier = Modifier
                .height(28.dp)
                .clearAndSetSemantics { },
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(14.dp))

        InfiniteScrollingText(
            text = scrollingMessage,
            modifier = Modifier
                .onGloballyPositioned {
                    spotlightFrames["scrolling_message"] = it.boundsInWindow()
                }
                .fillMaxWidth()
        )
    }
}
