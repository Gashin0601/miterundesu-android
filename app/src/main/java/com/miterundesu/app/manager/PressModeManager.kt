package com.miterundesu.app.manager

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.miterundesu.app.data.model.PressAccount
import com.miterundesu.app.data.remote.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.util.Date

class PressModeManager(
    private val context: Context,
    private val settingsManager: SettingsManager,
    private val localizationManager: LocalizationManager
) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isPressModeEnabled = MutableStateFlow(false)
    val isPressModeEnabled: StateFlow<Boolean> = _isPressModeEnabled.asStateFlow()

    private val _pressAccount = MutableStateFlow<PressAccount?>(null)
    val pressAccount: StateFlow<PressAccount?> = _pressAccount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val loginDateKey = "miterundesu.press.loginDate"

    private val loginPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("miterundesu_press", Context.MODE_PRIVATE)
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "com.miterundesu.press",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        attemptAutoLogin()
    }

    private fun attemptAutoLogin() {
        val savedUserId = encryptedPrefs.getString("userId", null)
        val savedPassword = encryptedPrefs.getString("password", null)
        if (savedUserId != null && savedPassword != null) {
            scope.launch {
                val success = performLogin(savedUserId, savedPassword, isAutoLogin = true)
                if (!success) {
                    clearCredentials()
                    _isPressModeEnabled.value = false
                    settingsManager.setPressMode(false)
                }
            }
        }
    }

    suspend fun login(userId: String, password: String): Boolean {
        return performLogin(userId, password, isAutoLogin = false)
    }

    private suspend fun performLogin(userId: String, password: String, isAutoLogin: Boolean): Boolean {
        _isLoading.value = true
        _error.value = null

        return try {
            val accounts = withContext(Dispatchers.IO) {
                val params = buildJsonObject {
                    put("p_user_id", userId)
                    put("p_password", password)
                }
                SupabaseClientProvider.client.postgrest.rpc(
                    "verify_press_account_password",
                    params
                ).decodeList<PressAccount>()
            }

            if (accounts.isEmpty()) {
                if (!isAutoLogin) {
                    _error.value = localizationManager.localizedString("press_login_error_invalid_credentials")
                }
                _isLoading.value = false
                return false
            }

            val account = accounts.first()

            if (!account.isValid) {
                if (!isAutoLogin) {
                    _error.value = when (account.status) {
                        com.miterundesu.app.data.model.PressAccountStatus.EXPIRED ->
                            localizationManager.localizedString("press_login_error_expired")
                        com.miterundesu.app.data.model.PressAccountStatus.DEACTIVATED ->
                            localizationManager.localizedString("press_login_error_deactivated")
                        else ->
                            localizationManager.localizedString("press_login_error_invalid")
                    }
                }
                _isLoading.value = false
                return false
            }

            _pressAccount.value = account
            _isLoggedIn.value = true
            _isPressModeEnabled.value = true
            settingsManager.setPressMode(true)

            saveCredentials(userId, password)
            recordLogin()

            updateLastLoginAt(userId)

            _isLoading.value = false
            true
        } catch (e: Exception) {
            Log.e("PressModeManager", "Login failed", e)
            if (!isAutoLogin) {
                _error.value = localizationManager.localizedString("press_login_error_failed")
            }
            _isLoading.value = false
            false
        }
    }

    private fun saveCredentials(userId: String, password: String) {
        encryptedPrefs.edit()
            .putString("userId", userId)
            .putString("password", password)
            .apply()
    }

    private fun clearCredentials() {
        encryptedPrefs.edit()
            .remove("userId")
            .remove("password")
            .apply()
    }

    private fun updateLastLoginAt(userId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                SupabaseClientProvider.client.postgrest["press_accounts"]
                    .update(
                        buildJsonObject {
                            put("last_login_at", Instant.now().toString())
                        }
                    ) {
                        filter {
                            eq("user_id", userId)
                        }
                    }
            } catch (e: Exception) {
                Log.e("PressModeManager", "Failed to update last_login_at", e)
            }
        }
    }

    fun logout() {
        _pressAccount.value = null
        _isLoggedIn.value = false
        _isPressModeEnabled.value = false
        _error.value = null
        settingsManager.setPressMode(false)
        clearCredentials()
        clearLoginRecord()
    }

    // MARK: - Login Record

    /// ログイン成功を記録
    private fun recordLogin() {
        loginPrefs.edit()
            .putLong(loginDateKey, Date().time)
            .apply()
    }

    /// ログイン記録をクリア
    private fun clearLoginRecord() {
        loginPrefs.edit()
            .remove(loginDateKey)
            .apply()
    }

    // MARK: - Account Info

    /// ログイン中のユーザーIDを取得
    fun getCurrentUserId(): String? {
        return _pressAccount.value?.userId
    }

    /// アカウント情報の概要を取得
    fun getAccountSummary(): String? {
        return _pressAccount.value?.summary
    }

    /// 有効期限までの残り日数を取得
    fun getDaysUntilExpiration(): Int? {
        return _pressAccount.value?.daysUntilExpiration
    }
}
