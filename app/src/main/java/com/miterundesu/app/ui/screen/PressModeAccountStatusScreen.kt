package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miterundesu.app.data.model.PressAccount
import com.miterundesu.app.data.model.PressAccountStatus
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.PressModeManager

// System grouped background equivalent for dark mode
private val SystemGroupedBackground = Color(0xFF1C1C1E)
private val SystemBackground = Color(0xFF2C2C2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PressModeAccountStatusScreen(
    pressModeManager: PressModeManager,
    localizationManager: LocalizationManager,
    onClose: () -> Unit
) {
    val account by pressModeManager.pressAccount.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = localizationManager.localizedString("press_account_status_title"),
                        color = Color.White
                    )
                },
                actions = {
                    TextButton(onClick = onClose) {
                        Text(
                            text = localizationManager.localizedString("close"),
                            color = Color(0xFF0A84FF) // iOS system blue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SystemGroupedBackground
                )
            )
        },
        containerColor = SystemGroupedBackground
    ) { paddingValues ->
        val currentAccount = account
        if (currentAccount == null) {
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Status icon - 80sp to match iOS 80pt
            val statusIcon: ImageVector
            val statusColor: Color
            val statusTitle: String
            when (currentAccount.status) {
                PressAccountStatus.ACTIVE -> {
                    statusIcon = Icons.Filled.CheckCircle
                    statusColor = Color(0xFF34C759) // iOS green
                    statusTitle = localizationManager.localizedString("press_account_status_active")
                }
                PressAccountStatus.EXPIRED -> {
                    statusIcon = Icons.Filled.Schedule
                    statusColor = Color(0xFFFF9500) // iOS orange
                    statusTitle = localizationManager.localizedString("press_account_status_expired")
                }
                PressAccountStatus.DEACTIVATED -> {
                    statusIcon = Icons.Filled.Cancel
                    statusColor = Color.Red
                    statusTitle = localizationManager.localizedString("press_account_status_deactivated")
                }
            }

            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status title and message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = statusTitle,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = currentAccount.statusMessage,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Information card
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            SystemBackground,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Info header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF0A84FF), // iOS system blue
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = localizationManager.localizedString("press_account_info"),
                            color = Color(0xFF0A84FF),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Info rows matching iOS set
                    AccountInfoRow(
                        label = localizationManager.localizedString("press_account_user_id"),
                        value = currentAccount.userId
                    )

                    AccountInfoRow(
                        label = localizationManager.localizedString("press_account_organization"),
                        value = currentAccount.organizationName
                    )

                    currentAccount.contactPerson?.let { contact ->
                        AccountInfoRow(
                            label = localizationManager.localizedString("press_account_contact"),
                            value = contact
                        )
                    }

                    AccountInfoRow(
                        label = localizationManager.localizedString("press_account_expiration"),
                        value = currentAccount.expirationDisplayString
                    )

                    currentAccount.approvalDisplayString?.let { approved ->
                        AccountInfoRow(
                            label = localizationManager.localizedString("press_account_approved_at"),
                            value = approved
                        )
                    }
                }
            }

            // Expired section
            if (currentAccount.status == PressAccountStatus.EXPIRED) {
                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = localizationManager.localizedString("press_account_expired_message"),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://miterundesu.jp/press")
                            )
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0A84FF), // Blue like iOS
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = localizationManager.localizedString("press_account_apply_page"),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AccountInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}
