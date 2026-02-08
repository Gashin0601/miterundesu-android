package com.miterundesu.app.ui.screen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miterundesu.app.data.model.PressAccount
import com.miterundesu.app.data.model.PressAccountStatus
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.PressModeManager
import com.miterundesu.app.ui.theme.DarkBackground
import com.miterundesu.app.ui.theme.MainGreen

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
                        text = localizationManager.localizedString("press_account_status"),
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = localizationManager.localizedString("close"),
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        val currentAccount = account
        if (currentAccount == null) {
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Status icon
            val statusIcon: ImageVector
            val statusColor: Color
            when (currentAccount.status) {
                PressAccountStatus.ACTIVE -> {
                    statusIcon = Icons.Filled.CheckCircle
                    statusColor = MainGreen
                }
                PressAccountStatus.EXPIRED -> {
                    statusIcon = Icons.Filled.AccessTime
                    statusColor = Color(0xFFFF9500)
                }
                PressAccountStatus.DEACTIVATED -> {
                    statusIcon = Icons.Filled.Cancel
                    statusColor = Color.Red
                }
            }

            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Info rows
            AccountInfoRows(currentAccount, localizationManager)

            // Renewal link for expired accounts
            if (currentAccount.status == PressAccountStatus.EXPIRED) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://miterundesu.jp/press"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = localizationManager.localizedString("press_renewal_link"),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AccountInfoRows(
    account: PressAccount,
    localizationManager: LocalizationManager
) {
    val rows = buildList {
        add(localizationManager.localizedString("press_account_organization") to account.organizationName)
        account.organizationType?.let {
            add(localizationManager.localizedString("press_account_type") to it)
        }
        account.contactPerson?.let {
            add(localizationManager.localizedString("press_account_contact") to it)
        }
        account.email?.let {
            add(localizationManager.localizedString("press_account_email") to it)
        }
        account.phone?.let {
            add(localizationManager.localizedString("press_account_phone") to it)
        }
        account.approvedBy?.let {
            add(localizationManager.localizedString("press_account_approved_by") to it)
        }
        account.approvedAt?.let {
            add(localizationManager.localizedString("press_account_approved_date") to it)
        }
        add(localizationManager.localizedString("press_account_expires") to account.expirationDisplayString)
        account.daysUntilExpiration?.let {
            add(localizationManager.localizedString("press_account_days_remaining") to "${it}")
        }
        add(localizationManager.localizedString("press_account_status") to account.statusMessage)
        account.lastLoginAt?.let {
            add(localizationManager.localizedString("press_account_last_login") to it)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        rows.forEachIndexed { index, (label, value) ->
            InfoRow(label = label, value = value)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.weight(0.4f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}
