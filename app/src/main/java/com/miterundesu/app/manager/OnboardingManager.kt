package com.miterundesu.app.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OnboardingManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _showWelcomeScreen = MutableStateFlow(false)
    val showWelcomeScreen: StateFlow<Boolean> = _showWelcomeScreen.asStateFlow()

    private val _showFeatureHighlights = MutableStateFlow(false)
    val showFeatureHighlights: StateFlow<Boolean> = _showFeatureHighlights.asStateFlow()

    private val _showCompletionScreen = MutableStateFlow(false)
    val showCompletionScreen: StateFlow<Boolean> = _showCompletionScreen.asStateFlow()

    private val _isOnboardingActive = MutableStateFlow(false)
    val isOnboardingActive: StateFlow<Boolean> = _isOnboardingActive.asStateFlow()

    val hasCompletedOnboarding: Boolean
        get() = prefs.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)

    init {
        checkOnboardingStatus()
    }

    fun checkOnboardingStatus() {
        if (!hasCompletedOnboarding) {
            _isOnboardingActive.value = true
            _showWelcomeScreen.value = true
            _showFeatureHighlights.value = false
            _showCompletionScreen.value = false
        } else {
            _isOnboardingActive.value = false
            _showWelcomeScreen.value = false
            _showFeatureHighlights.value = false
            _showCompletionScreen.value = false
        }
    }

    fun completeWelcomeScreen() {
        _showWelcomeScreen.value = false
        _showFeatureHighlights.value = true
    }

    fun completeFeatureHighlights() {
        _showFeatureHighlights.value = false
        _showCompletionScreen.value = true
    }

    fun completeOnboarding() {
        _showCompletionScreen.value = false
        _showWelcomeScreen.value = false
        _showFeatureHighlights.value = false
        _isOnboardingActive.value = false
        prefs.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true).apply()
    }

    fun skipOnboarding() {
        completeOnboarding()
    }

    fun showTutorial() {
        _isOnboardingActive.value = true
        _showWelcomeScreen.value = true
        _showFeatureHighlights.value = false
        _showCompletionScreen.value = false
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    }
}
