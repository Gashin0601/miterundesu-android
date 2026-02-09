package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.ui.component.TheaterModeToggle
import com.miterundesu.app.ui.theme.CardBackground
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
    pressAccountStatus: String? = null,
    pressAccountOrganization: String? = null,
    pressAccountUserId: String? = null,
    pressAccountExpiration: String? = null,
    pressAccountDaysUntilExpiration: Int? = null,
    isConnected: Boolean,
    versionName: String,
    onBack: () -> Unit,
    onTheaterToggle: () -> Unit,
    onZoomChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onScrollingMessageNormalChange: (String) -> Unit,
    onScrollingMessageTheaterChange: (String) -> Unit,
    onPressModeToggle: () -> Unit = {},
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
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val closeIconSize = (screenWidthDp * 0.07f).dp

    var showResetDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var localZoom by remember(maxZoomFactor) { mutableFloatStateOf(maxZoomFactor) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(accentColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        TopAppBar(
            title = {
                // Centered title (matching iOS inline navigationTitle)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localizationManager.localizedString("settings_title"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                // Theater mode toggle on the LEFT side (matching iOS navigationBarLeading)
                TheaterModeToggle(
                    isTheaterMode = isTheaterMode,
                    onToggle = onTheaterToggle,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            actions = {
                // Close button on the RIGHT side (matching iOS navigationBarTrailing xmark.circle.fill)
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = localizationManager.localizedString("back"),
                        tint = Color.White,
                        modifier = Modifier.size(closeIconSize)
                    )
                }
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
                        modifier = Modifier
                            .padding(12.dp)
                            .semantics(mergeDescendants = true) { },
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
            SettingsCard(isTheaterMode = isTheaterMode) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("max_zoom"),
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "\u00D7${localZoom.roundToInt()}",
                            color = Color.White,
                            fontSize = 17.sp,
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
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        ),
                        track = { sliderState ->
                            SliderDefaults.Track(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color.White,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                thumbTrackGapSize = 0.dp,
                                drawStopIndicator = null
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("\u00D710", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("\u00D7200", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizationManager.localizedString("camera_zoom_description"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("language_setting"))
            SettingsCard(isTheaterMode = isTheaterMode) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SegmentedButton(
                        selected = language == "ja",
                        onClick = { onLanguageChange("ja") },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = accentColor,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = Color.White.copy(alpha = 0.7f),
                            activeBorderColor = Color.White.copy(alpha = 0.3f),
                            inactiveBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        icon = {}
                    ) {
                        Text(
                            localizationManager.localizedString("language_japanese"),
                            fontWeight = if (language == "ja") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    SegmentedButton(
                        selected = language == "en",
                        onClick = { onLanguageChange("en") },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = accentColor,
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.Transparent,
                            inactiveContentColor = Color.White.copy(alpha = 0.7f),
                            activeBorderColor = Color.White.copy(alpha = 0.3f),
                            inactiveBorderColor = Color.White.copy(alpha = 0.3f)
                        ),
                        icon = {}
                    ) {
                        Text(
                            localizationManager.localizedString("language_english"),
                            fontWeight = if (language == "en") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("scrolling_message"))
            SettingsCard(isTheaterMode = isTheaterMode) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show mode badge and only the active mode's field (matching iOS)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("message_content"),
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isTheaterMode) localizationManager.localizedString("theater_mode_label") else localizationManager.localizedString("normal_mode_label"),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    // Only show the field for the current mode (matching iOS TextEditor with minHeight:100, maxHeight:200)
                    if (isTheaterMode) {
                        OutlinedTextField(
                            value = scrollingMessageTheater,
                            onValueChange = { text ->
                                onScrollingMessageTheaterChange(text.replace("\n", "").replace("\r", ""))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = accentColor
                            ),
                            singleLine = false
                        )
                    } else {
                        OutlinedTextField(
                            value = scrollingMessageNormal,
                            onValueChange = { text ->
                                onScrollingMessageNormalChange(text.replace("\n", "").replace("\r", ""))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 200.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentColor,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = accentColor
                            ),
                            singleLine = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("press_mode_section"))
            SettingsCard(isTheaterMode = isTheaterMode) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Press mode account status section (matching iOS)
                    if (isLoggedIn && pressAccountSummary != null) {
                        // Status icon and title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics(mergeDescendants = true) { }
                        ) {
                            val statusIcon = when (pressAccountStatus) {
                                "active" -> Icons.Default.CheckCircle
                                "expired" -> Icons.Default.Schedule
                                else -> Icons.Default.Shield
                            }
                            val statusIconColor = when (pressAccountStatus) {
                                "active" -> Color(0xFF34C759)
                                "expired" -> Color(0xFFFF9500)
                                else -> Color.Red
                            }
                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusIconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pressAccountSummary,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Organization name
                        if (pressAccountOrganization != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = pressAccountOrganization,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 15.sp
                            )
                        }
                        // User ID
                        if (pressAccountUserId != null) {
                            Text(
                                text = "${localizationManager.localizedString("press_account_user_id")}: $pressAccountUserId",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        // Expiration date
                        if (pressAccountExpiration != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${localizationManager.localizedString("expiration_date")}: $pressAccountExpiration",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 15.sp
                            )
                        }
                        // Expiring soon warning
                        if (pressAccountStatus == "active" && pressAccountDaysUntilExpiration != null && pressAccountDaysUntilExpiration < 30) {
                            Text(
                                text = localizationManager.localizedString("press_mode_status_expires_soon").replace("{days}", pressAccountDaysUntilExpiration.toString()),
                                color = Color.Yellow,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Logout button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Red.copy(alpha = if (isConnected) 0.3f else 0.15f))
                                .clickable(enabled = isConnected) { showLogoutDialog = true }
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .then(
                                    if (!isConnected) Modifier.alpha(0.5f) else Modifier
                                )
                                .semantics(mergeDescendants = true) {
                                    contentDescription = if (!isConnected) {
                                        "${localizationManager.localizedString("press_logout")}, ${localizationManager.localizedString("offline_warning")}"
                                    } else {
                                        localizationManager.localizedString("press_logout")
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localizationManager.localizedString("press_logout"),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // Not logged in section (matching iOS)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics(mergeDescendants = true) { }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clearAndSetSemantics { }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localizationManager.localizedString("press_not_logged_in"),
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = localizationManager.localizedString("press_apply_description"),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Website link button (matching iOS)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/press"))
                                    context.startActivity(intent)
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .semantics(mergeDescendants = true) {
                                    contentDescription = localizationManager.localizedString("press_apply_button")
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clearAndSetSemantics { }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizationManager.localizedString("press_apply_button"),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "miterundesu.jp/press",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "\u2197",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.clearAndSetSemantics { }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Press mode toggle (matching iOS custom toggle)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .let { mod ->
                                if (isConnected) {
                                    mod.clickable {
                                        if (isLoggedIn) {
                                            onPressModeToggle()
                                        } else {
                                            onPressModeLoginClick()
                                        }
                                    }
                                } else {
                                    mod
                                }
                            }
                            .semantics(mergeDescendants = true) {
                                role = Role.Switch
                                stateDescription = if (isPressMode) localizationManager.localizedString("on") else localizationManager.localizedString("off")
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = localizationManager.localizedString("press_mode_label"),
                            color = Color.White.copy(alpha = if (isConnected) 1f else 0.5f),
                            fontSize = 15.sp
                        )
                        // Custom toggle (matching iOS)
                        val toggleOffset by animateFloatAsState(
                            targetValue = if (isPressMode) 10f else -10f,
                            label = "toggle"
                        )
                        Box(
                            modifier = Modifier.size(width = 51.dp, height = 31.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 51.dp, height = 31.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isPressMode) Color.Red.copy(alpha = 0.6f)
                                        else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .size(27.dp)
                                    .offset(x = toggleOffset.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizationManager.localizedString("press_mode_description"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )

                    // Offline warning
                    if (!isConnected) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics(mergeDescendants = true) { }
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isTheaterMode) Color.Red else Color(0xFFFF9500),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = localizationManager.localizedString("offline_warning"),
                                color = if (isTheaterMode) Color.Red else Color(0xFFFF9500),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            SectionHeader(text = localizationManager.localizedString("app_info"))
            SettingsCard(isTheaterMode = isTheaterMode) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Version (matching iOS order: Version -> Official Site -> Show Tutorial -> Privacy Policy -> Terms of Service)
                    InfoRow(
                        label = localizationManager.localizedString("version"),
                        value = versionName,
                        modifier = Modifier.semantics(mergeDescendants = true) {
                            contentDescription = "${localizationManager.localizedString("version_info")} $versionName"
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Official site link (matching iOS - with arrow icon)
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = localizationManager.localizedString("official_site")
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(localizationManager.localizedString("explanation_website"), color = Color.White)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Tutorial
                    TextButton(
                        onClick = onShowTutorial,
                        enabled = !isTheaterMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = localizationManager.localizedString("show_tutorial")
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    localizationManager.localizedString("tutorial_show"),
                                    color = if (isTheaterMode) Color.White.copy(alpha = 0.5f) else Color.White
                                )
                                if (isTheaterMode) {
                                    Text(
                                        localizationManager.localizedString("tutorial_unavailable_theater"),
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = if (isTheaterMode) 0.5f else 1f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Privacy policy
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/privacy"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = localizationManager.localizedString("privacy_policy")
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(localizationManager.localizedString("privacy_policy"), color = Color.White)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Terms of service
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/terms"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                contentDescription = localizationManager.localizedString("terms_of_service")
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(localizationManager.localizedString("terms_of_service"), color = Color.White)
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            // Reset button: normal card style (matching iOS Form row style)
            SettingsCard(isTheaterMode = isTheaterMode) {
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = localizationManager.localizedString("reset_settings"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(localizationManager.localizedString("reset_confirm_title")) },
            text = { Text(localizationManager.localizedString("reset_confirm_message")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        onResetSettings()
                    }
                ) {
                    Text(localizationManager.localizedString("reset_confirm_button"), color = Color.Red)
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
            title = { Text(localizationManager.localizedString("logout_confirm_title")) },
            text = { Text(localizationManager.localizedString("logout_confirm_message")) },
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
        color = Color.White.copy(alpha = 0.8f),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
private fun SettingsCard(
    isTheaterMode: Boolean = false,
    content: @Composable () -> Unit
) {
    // iOS: Color(red: 0.95, green: 0.6, blue: 0.3, opacity: 0.35) for orange
    // iOS: Color(red: 0.2, green: 0.6, blue: 0.4, opacity: 0.35) for green
    val bgColor = if (isTheaterMode)
        Color(0xFFF2994D).copy(alpha = 0.35f)
    else
        Color(0xFF339966).copy(alpha = 0.35f)
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp
        )
    }
}
