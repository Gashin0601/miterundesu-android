package com.miterundesu.app.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _showWelcomeScreen = MutableStateFlow(false)
    val showWelcomeScreen: StateFlow<Boolean> = _showWelcomeScreen.asStateFlow()

    private val _showFeatureHighlights = MutableStateFlow(false)
    val showFeatureHighlights: StateFlow<Boolean> = _showFeatureHighlights.asStateFlow()

    private val _showCompletionScreen = MutableStateFlow(false)
    val showCompletionScreen: StateFlow<Boolean> = _showCompletionScreen.asStateFlow()

    private val _currentHighlightedIDs = MutableStateFlow<Set<String>>(emptySet())
    val currentHighlightedIDs: StateFlow<Set<String>> = _currentHighlightedIDs.asStateFlow()

    init {
        _hasCompletedOnboarding.value = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
    }

    /// アプリ起動時に呼び出して、オンボーディングを表示するかチェック
    fun checkOnboardingStatus() {
        if (!_hasCompletedOnboarding.value) {
            _showWelcomeScreen.value = true
        }
    }

    /// ウェルカム画面完了後、機能ハイライトを表示
    fun completeWelcomeScreen() {
        _showWelcomeScreen.value = false
        _showFeatureHighlights.value = true
    }

    /// 機能ハイライト完了後、完了画面を表示
    fun completeFeatureHighlights() {
        _showFeatureHighlights.value = false
        _showCompletionScreen.value = true
    }

    /// オンボーディング全体を完了（完了画面の「使い始める」ボタンから）
    fun completeOnboarding() {
        _showWelcomeScreen.value = false
        _showFeatureHighlights.value = false
        _showCompletionScreen.value = false
        setHasCompletedOnboarding(true)
    }

    /// オンボーディングを最初からやり直す（設定画面から呼び出し用）
    fun resetOnboarding() {
        setHasCompletedOnboarding(false)
        _showWelcomeScreen.value = true
    }

    /// チュートリアルを最初から表示（設定画面から呼び出し用）
    /// 必ずウェルカム画面から開始し、「始める」ボタンでハイライトに進む
    fun showTutorial() {
        _showWelcomeScreen.value = true
    }

    private fun setHasCompletedOnboarding(value: Boolean) {
        _hasCompletedOnboarding.value = value
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, value).apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    }
}
