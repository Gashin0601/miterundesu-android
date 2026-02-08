package com.miterundesu.app.manager

import android.app.Activity
import android.os.Build
import android.view.Window
import android.view.WindowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SecurityManager {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _hideContent = MutableStateFlow(false)
    val hideContent: StateFlow<Boolean> = _hideContent.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _showScreenshotWarning = MutableStateFlow(false)
    val showScreenshotWarning: StateFlow<Boolean> = _showScreenshotWarning.asStateFlow()

    private val _showRecordingWarning = MutableStateFlow(false)
    val showRecordingWarning: StateFlow<Boolean> = _showRecordingWarning.asStateFlow()

    var isPressModeEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                currentWindow?.let { disableSecurity(it) }
                _hideContent.value = false
                _isRecording.value = false
                _showScreenshotWarning.value = false
                _showRecordingWarning.value = false
            } else {
                currentWindow?.let { enableSecurity(it) }
            }
        }

    private var currentWindow: Window? = null
    private var hideContentJob: Job? = null
    private var warningDismissJob: Job? = null
    private var screenCaptureCallback: Any? = null

    fun enableSecurity(window: Window) {
        currentWindow = window
        if (isPressModeEnabled) return

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        registerScreenCaptureCallback(window)
    }

    fun disableSecurity(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        unregisterScreenCaptureCallback(window)
    }

    private fun registerScreenCaptureCallback(window: Window) {
        if (Build.VERSION.SDK_INT >= 34) {
            val activity = window.context as? Activity ?: return
            val callback = Activity.ScreenCaptureCallback {
                onScreenshotDetected()
            }
            screenCaptureCallback = callback
            activity.registerScreenCaptureCallback(
                activity.mainExecutor,
                callback
            )
        }
    }

    private fun unregisterScreenCaptureCallback(window: Window) {
        if (Build.VERSION.SDK_INT >= 34) {
            val activity = window.context as? Activity ?: return
            val callback = screenCaptureCallback as? Activity.ScreenCaptureCallback
            if (callback != null) {
                activity.unregisterScreenCaptureCallback(callback)
                screenCaptureCallback = null
            }
        }
    }

    private fun onScreenshotDetected() {
        if (isPressModeEnabled) return

        _hideContent.value = true
        _showScreenshotWarning.value = true

        hideContentJob?.cancel()
        hideContentJob = scope.launch {
            delay(3000L)
            _hideContent.value = false
        }

        warningDismissJob?.cancel()
        warningDismissJob = scope.launch {
            delay(3000L)
            _showScreenshotWarning.value = false
        }
    }

    fun setRecordingDetected(recording: Boolean) {
        if (isPressModeEnabled) {
            _isRecording.value = false
            return
        }
        _isRecording.value = recording
        _showRecordingWarning.value = recording
        if (recording) {
            _hideContent.value = true
        } else {
            _hideContent.value = false
        }
    }

    fun clearSensitiveData() {
        _hideContent.value = false
        _isRecording.value = false
        _showScreenshotWarning.value = false
        _showRecordingWarning.value = false
    }

    fun release() {
        currentWindow?.let { unregisterScreenCaptureCallback(it) }
        hideContentJob?.cancel()
        warningDismissJob?.cancel()
        currentWindow = null
        scope.cancel()
    }
}
