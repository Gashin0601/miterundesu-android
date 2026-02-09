package com.miterundesu.app.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WhatsNewManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val currentVersion = "1.1.0"

    private val _shouldShowWhatsNew = MutableStateFlow(false)
    val shouldShowWhatsNew: StateFlow<Boolean> = _shouldShowWhatsNew.asStateFlow()

    init {
        checkForNewVersion()
    }

    /// 新しいバージョンかどうかをチェック
    fun checkForNewVersion() {
        val lastSeenVersion = prefs.getString(KEY_LAST_SEEN_APP_VERSION, null)

        if (lastSeenVersion != null) {
            // バージョンが異なる場合、かつ1.1.0へのアップデートの場合
            if (lastSeenVersion != currentVersion && currentVersion == "1.1.0") {
                _shouldShowWhatsNew.value = true
            }
        } else {
            // 初回起動 - バージョンを保存するだけ
            prefs.edit().putString(KEY_LAST_SEEN_APP_VERSION, currentVersion).apply()
        }
    }

    /// 新機能案内を表示済みとしてマーク
    fun markWhatsNewAsSeen() {
        prefs.edit().putString(KEY_LAST_SEEN_APP_VERSION, currentVersion).apply()
        _shouldShowWhatsNew.value = false
    }

    companion object {
        private const val PREFS_NAME = "whats_new_prefs"
        private const val KEY_LAST_SEEN_APP_VERSION = "last_seen_app_version"
    }
}
