package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.component.TheaterModeToggle
import com.miterundesu.app.ui.theme.CardBackground
import com.miterundesu.app.ui.theme.DarkBackground
import com.miterundesu.app.ui.theme.MainGreen
import com.miterundesu.app.ui.theme.TheaterOrange
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    localizationManager: LocalizationManager,
    isTheaterMode: Boolean,
    maxZoomFactor: Float,
    language: String,
    scrollingMessageNormal: String,
    scrollingMessageTheater: String,
    isPressMode: Boolean,
    isLoggedIn: Boolean,
    pressAccountSummary: String?,
    isConnected: Boolean,
    versionName: String,
    onBack: () -> Unit,
    onTheaterToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onScrollingMessageNormalChange: (String) -> Unit,
    onScrollingMessageTheaterChange: (String) -> Unit,
    onPressModeLoginClick: () -> Unit,
    onPressModeInfoClick: () -> Unit,
    onPressModeAccountStatusClick: () -> Unit,
    onLogout: () -> Unit,
    onShowTutorial: () -> Unit,
    onResetSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val accentColor = if (isTheaterMode) TheaterOrange else MainGreen

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var localZoom by remember(maxZoomFactor) { mutableFloatStateOf(maxZoomFactor) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = localizationManager.localizedString("settings_title"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localizationManager.localizedString("back"),
                        tint = Color.White
                    )
                }
            },
            actions = {
                TheaterModeToggle(
                    isTheaterMode = isTheaterMode,
                    onToggle = onTheaterToggle,
                    modifier = Modifier.padding(end = 12.dp)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (!isConnected) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3A2A00)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Color(0xFFFFA000)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = localizationManager.localizedString("offline_warning"),
                            color = Color(0xFFFFA000),
                            fontSize = 13.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            SectionHeader(text = localizationManager.localizedString("camera_settings"))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("zoom_range"),
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${localZoom.roundToInt()}x",
                            color = accentColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = localZoom,
                        onValueChange = { newValue ->
                            val stepped = (newValue / 10f).roundToInt() * 10f
                            localZoom = stepped.coerceIn(10f, 200f)
                        },
                        onValueChangeFinished = {
                            onZoomChange(localZoom)
                        },
                        valueRange = 10f..200f,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10x", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                        Text("200x", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("language_setting"))
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = language == "ja",
                        onClick = { onLanguageChange("ja") },
                        label = {
                            Text(
                                localizationManager.localizedString("language_japanese"),
                                fontWeight = if (language == "ja") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White,
                            containerColor = CardBackground,
                            labelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = language == "en",
                        onClick = { onLanguageChange("en") },
                        label = {
                            Text(
                                localizationManager.localizedString("language_english"),
                                fontWeight = if (language == "en") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor,
                            selectedLabelColor = Color.White,
                            containerColor = CardBackground,
                            labelColor = Color.White.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("scrolling_message"))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = localizationManager.localizedString("scrolling_message_normal"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scrollingMessageNormal,
                        onValueChange = { text ->
                            onScrollingMessageNormalChange(text.replace("\n", ""))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = accentColor
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = localizationManager.localizedString("scrolling_message_theater"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scrollingMessageTheater,
                        onValueChange = { text ->
                            onScrollingMessageTheaterChange(text.replace("\n", ""))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TheaterOrange,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = TheaterOrange
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("press_mode_section"))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isLoggedIn && pressAccountSummary != null) {
                        Text(
                            text = pressAccountSummary,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onPressModeAccountStatusClick) {
                            Text(localizationManager.localizedString("press_account_status"), color = accentColor)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showLogoutDialog = true }) {
                            Text(localizationManager.localizedString("press_logout"), color = Color.Red)
                        }
                    } else {
                        TextButton(
                            onClick = onPressModeLoginClick,
                            enabled = isConnected
                        ) {
                            Text(localizationManager.localizedString("press_login_button"), color = if (isConnected) accentColor else Color.Gray)
                        }
                        TextButton(onClick = onPressModeInfoClick) {
                            Text(localizationManager.localizedString("press_info_title"), color = accentColor)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("app_info"))
            SettingsCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(label = localizationManager.localizedString("version"), value = versionName)
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/terms"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(localizationManager.localizedString("terms_of_service"), color = accentColor)
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/privacy"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(localizationManager.localizedString("privacy_policy"), color = accentColor)
                    }
                    TextButton(onClick = onShowTutorial) {
                        Text(localizationManager.localizedString("tutorial_show"), color = accentColor)
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp"))
                            context.startActivity(intent)
                        }
                    ) {
                        Text(localizationManager.localizedString("explanation_website"), color = accentColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("reset_settings"))
            Button(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.15f),
                    contentColor = Color.Red
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = localizationManager.localizedString("reset_settings"),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Text(
                text = "\u00A9 2024 Miterundesu",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(localizationManager.localizedString("reset_settings")) },
            text = { Text(localizationManager.localizedString("reset_confirm_message")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetSettings()
                    }
                ) {
                    Text(localizationManager.localizedString("reset_settings"), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(localizationManager.localizedString("cancel"))
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(localizationManager.localizedString("press_logout")) },
            text = { Text(localizationManager.localizedString("press_logout_confirm")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) {
                    Text(localizationManager.localizedString("press_logout"), color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(localizationManager.localizedString("cancel"))
                }
            },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.6f),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
