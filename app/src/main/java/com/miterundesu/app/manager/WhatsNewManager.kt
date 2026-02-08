package com.miterundesu.app.manager

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager

class WhatsNewManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val lastSeenAppVersion: String?
        get() = prefs.getString(KEY_LAST_SEEN_APP_VERSION, null)

    private val currentAppVersion: String
        get() = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: CURRENT_VERSION
        } catch (_: PackageManager.NameNotFoundException) {
            CURRENT_VERSION
        }

    val shouldShowWhatsNew: Boolean
        get() {
            val lastSeen = lastSeenAppVersion
            // First install: don't show What's New
            if (lastSeen == null) return false
            // Only show when upgrading to 1.1.0 from a lower version
            return compareVersions(lastSeen, TARGET_VERSION) < 0 &&
                    compareVersions(currentAppVersion, TARGET_VERSION) >= 0
        }

    fun markAsSeen() {
        prefs.edit().putString(KEY_LAST_SEEN_APP_VERSION, currentAppVersion).apply()
    }

    fun recordCurrentVersion() {
        if (lastSeenAppVersion == null) {
            prefs.edit().putString(KEY_LAST_SEEN_APP_VERSION, currentAppVersion).apply()
        }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(parts1.size, parts2.size)
        for (i in 0 until maxLength) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1.compareTo(p2)
        }
        return 0
    }

    companion object {
        private const val PREFS_NAME = "whats_new_prefs"
        private const val KEY_LAST_SEEN_APP_VERSION = "last_seen_app_version"
        private const val TARGET_VERSION = "1.1.0"
        private const val CURRENT_VERSION = "1.1.0"
    }
}
