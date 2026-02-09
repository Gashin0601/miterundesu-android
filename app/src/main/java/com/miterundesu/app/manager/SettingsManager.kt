package com.miterundesu.app.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _maxZoomFactor = MutableStateFlow(DEFAULT_MAX_ZOOM_FACTOR)
    val maxZoomFactor: StateFlow<Float> = _maxZoomFactor.asStateFlow()

    private val _language = MutableStateFlow(DEFAULT_LANGUAGE)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _isTheaterMode = MutableStateFlow(DEFAULT_IS_THEATER_MODE)
    val isTheaterMode: StateFlow<Boolean> = _isTheaterMode.asStateFlow()

    private val _isPressMode = MutableStateFlow(DEFAULT_IS_PRESS_MODE)
    val isPressMode: StateFlow<Boolean> = _isPressMode.asStateFlow()

    private val _scrollingMessageNormal = MutableStateFlow(DEFAULT_SCROLLING_MESSAGE_NORMAL)
    val scrollingMessageNormal: StateFlow<String> = _scrollingMessageNormal.asStateFlow()

    private val _scrollingMessageTheater = MutableStateFlow(DEFAULT_SCROLLING_MESSAGE_THEATER)
    val scrollingMessageTheater: StateFlow<String> = _scrollingMessageTheater.asStateFlow()

    // 現在のモードに応じたスクロールメッセージを返す
    val scrollingMessage: String
        get() = if (_isTheaterMode.value) _scrollingMessageTheater.value else _scrollingMessageNormal.value

    init {
        scope.launch {
            loadSettings()
        }
    }

    private suspend fun loadSettings() {
        val prefs = context.dataStore.data.first()
        _maxZoomFactor.value = prefs[KEY_MAX_ZOOM_FACTOR] ?: DEFAULT_MAX_ZOOM_FACTOR
        _language.value = prefs[KEY_LANGUAGE] ?: DEFAULT_LANGUAGE
        _isTheaterMode.value = prefs[KEY_IS_THEATER_MODE] ?: DEFAULT_IS_THEATER_MODE
        _isPressMode.value = prefs[KEY_IS_PRESS_MODE] ?: DEFAULT_IS_PRESS_MODE
        _scrollingMessageNormal.value = prefs[KEY_SCROLLING_MESSAGE_NORMAL] ?: DEFAULT_SCROLLING_MESSAGE_NORMAL
        _scrollingMessageTheater.value = prefs[KEY_SCROLLING_MESSAGE_THEATER] ?: DEFAULT_SCROLLING_MESSAGE_THEATER
    }

    fun setMaxZoomFactor(value: Float) {
        val clamped = value.coerceIn(MIN_ZOOM_FACTOR, MAX_ZOOM_FACTOR)
        _maxZoomFactor.value = clamped
        scope.launch {
            context.dataStore.edit { it[KEY_MAX_ZOOM_FACTOR] = clamped }
        }
    }

    fun setLanguage(value: String) {
        val lang = if (value == "en") "en" else "ja"
        _language.value = lang
        scope.launch {
            context.dataStore.edit { it[KEY_LANGUAGE] = lang }
        }
    }

    fun setTheaterMode(value: Boolean) {
        _isTheaterMode.value = value
        scope.launch {
            context.dataStore.edit { it[KEY_IS_THEATER_MODE] = value }
        }
    }

    fun setPressMode(value: Boolean) {
        _isPressMode.value = value
        scope.launch {
            context.dataStore.edit { it[KEY_IS_PRESS_MODE] = value }
        }
    }

    fun setScrollingMessageNormal(value: String) {
        val cleaned = value.replace("\n", "").replace("\r", "")
        _scrollingMessageNormal.value = cleaned
        scope.launch {
            context.dataStore.edit { it[KEY_SCROLLING_MESSAGE_NORMAL] = cleaned }
        }
    }

    fun setScrollingMessageTheater(value: String) {
        val cleaned = value.replace("\n", "").replace("\r", "")
        _scrollingMessageTheater.value = cleaned
        scope.launch {
            context.dataStore.edit { it[KEY_SCROLLING_MESSAGE_THEATER] = cleaned }
        }
    }

    fun resetToDefaults() {
        _maxZoomFactor.value = DEFAULT_MAX_ZOOM_FACTOR
        _language.value = DEFAULT_LANGUAGE
        _isTheaterMode.value = DEFAULT_IS_THEATER_MODE
        _isPressMode.value = DEFAULT_IS_PRESS_MODE
        _scrollingMessageNormal.value = DEFAULT_SCROLLING_MESSAGE_NORMAL
        _scrollingMessageTheater.value = DEFAULT_SCROLLING_MESSAGE_THEATER
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[KEY_MAX_ZOOM_FACTOR] = DEFAULT_MAX_ZOOM_FACTOR
                prefs[KEY_LANGUAGE] = DEFAULT_LANGUAGE
                prefs[KEY_IS_THEATER_MODE] = DEFAULT_IS_THEATER_MODE
                prefs[KEY_IS_PRESS_MODE] = DEFAULT_IS_PRESS_MODE
                prefs[KEY_SCROLLING_MESSAGE_NORMAL] = DEFAULT_SCROLLING_MESSAGE_NORMAL
                prefs[KEY_SCROLLING_MESSAGE_THEATER] = DEFAULT_SCROLLING_MESSAGE_THEATER
            }
        }
    }

    companion object {
        private val KEY_MAX_ZOOM_FACTOR = floatPreferencesKey("max_zoom_factor")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_IS_THEATER_MODE = booleanPreferencesKey("is_theater_mode")
        private val KEY_IS_PRESS_MODE = booleanPreferencesKey("is_press_mode")
        private val KEY_SCROLLING_MESSAGE_NORMAL = stringPreferencesKey("scrolling_message_normal")
        private val KEY_SCROLLING_MESSAGE_THEATER = stringPreferencesKey("scrolling_message_theater")

        const val MIN_ZOOM_FACTOR = 10f
        const val MAX_ZOOM_FACTOR = 200f
        const val DEFAULT_MAX_ZOOM_FACTOR = 100f
        const val DEFAULT_LANGUAGE = "ja"
        const val DEFAULT_IS_THEATER_MODE = false
        const val DEFAULT_IS_PRESS_MODE = false
        const val DEFAULT_SCROLLING_MESSAGE_NORMAL = "撮影・録画は行っていません。スマートフォンを拡大鏡として使っています。画像は一時的に保存できますが、10分後には自動的に削除されます。共有やスクリーンショットはできません。"
        const val DEFAULT_SCROLLING_MESSAGE_THEATER = "撮影・録画は行っていません。スマートフォンを拡大鏡として使用しています。スクリーンショットや画面収録を含め、一切の保存ができないカメラアプリですので、ご安心ください。"
    }
}

// MARK: - Language
enum class Language(val code: String) {
    JAPANESE("ja"),
    ENGLISH("en");

    val displayName: String
        get() = when (this) {
            JAPANESE -> "日本語"
            ENGLISH -> "English"
        }
}
