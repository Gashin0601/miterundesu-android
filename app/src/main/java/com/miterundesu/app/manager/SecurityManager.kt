package com.miterundesu.app.manager

import android.app.Activity
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SecurityManager {

    companion object {
        private const val TAG = "SecurityManager"
        private const val RECORDING_POLL_INTERVAL_MS = 100L
    }

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

    private var currentWindow: Window? = null
    private var hideContentJob: Job? = null
    private var warningDismissJob: Job? = null
    private var screenCaptureCallback: Any? = null

    // Recording detection state
    private var recordingPollingJob: Job? = null
    private var displayManager: DisplayManager? = null
    private var displayListener: DisplayManager.DisplayListener? = null

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
            try {
                val activity = window.context as? Activity ?: return
                val callback = Activity.ScreenCaptureCallback {
                    onScreenshotDetected()
                }
                screenCaptureCallback = callback
                activity.registerScreenCaptureCallback(
                    activity.mainExecutor,
                    callback
                )
            } catch (e: SecurityException) {
                Log.w(TAG, "Cannot register screen capture callback: ${e.message}")
            }
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
            _showRecordingWarning.value = false
            return
        }
        _isRecording.value = recording
        _showRecordingWarning.value = recording
    }

    fun dismissScreenshotWarning() {
        _showScreenshotWarning.value = false
    }

    fun clearSensitiveData() {
        Log.d(TAG, "Clearing sensitive data")
    }

    // --- Screen recording detection ---

    /**
     * Starts screen recording detection. Should be called from Activity.onCreate().
     *
     * Uses two complementary mechanisms:
     * 1. DisplayManager.DisplayListener - reacts to display added/removed/changed events
     * 2. Coroutine polling loop (100ms interval) - catches recording that doesn't trigger
     *    display events, matching iOS's 0.1s polling timer
     *
     * On API 34+, the ScreenCaptureCallback registered in enableSecurity() also contributes.
     */
    fun startRecordingDetection(context: Context) {
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager ?: return
        displayManager = dm

        // Register DisplayListener for immediate reaction to display changes
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                checkRecordingState(dm)
            }

            override fun onDisplayRemoved(displayId: Int) {
                checkRecordingState(dm)
            }

            override fun onDisplayChanged(displayId: Int) {
                checkRecordingState(dm)
            }
        }
        displayListener = listener
        dm.registerDisplayListener(listener, null)

        // Initial check
        checkRecordingState(dm)

        // Start polling loop (100ms, matching iOS's 0.1s timer)
        recordingPollingJob?.cancel()
        recordingPollingJob = scope.launch {
            while (isActive) {
                checkRecordingState(dm)
                delay(RECORDING_POLL_INTERVAL_MS)
            }
        }

        Log.d(TAG, "Screen recording detection started")
    }

    /**
     * Stops screen recording detection. Should be called from Activity.onDestroy().
     */
    fun stopRecordingDetection() {
        recordingPollingJob?.cancel()
        recordingPollingJob = null

        displayListener?.let { listener ->
            displayManager?.unregisterDisplayListener(listener)
        }
        displayListener = null
        displayManager = null

        Log.d(TAG, "Screen recording detection stopped")
    }

    /**
     * Checks whether screen recording or screen mirroring/casting is currently active.
     *
     * Detection approach:
     * - Checks for presentation displays (screen mirroring/casting) via DISPLAY_CATEGORY_PRESENTATION
     * - Checks all displays for virtual display flags (FLAG_PRESENTATION, FLAG_PRIVATE)
     *   which indicate screen recording, virtual displays created by recording apps, or casting
     * - On API 34+, also uses WindowManager.addScreenRecordingCallback (via ScreenCaptureCallback)
     */
    private fun checkRecordingState(dm: DisplayManager) {
        val isRecordingDetected = detectScreenRecording(dm)

        // Only update state if it changed (avoid unnecessary recompositions)
        if (_isRecording.value != isRecordingDetected) {
            if (isRecordingDetected) {
                Log.d(TAG, "Screen recording/mirroring detected")
            } else {
                Log.d(TAG, "Screen recording/mirroring stopped")
            }
            setRecordingDetected(isRecordingDetected)
        }
    }

    /**
     * Performs the actual detection logic. Returns true if screen recording or
     * screen mirroring/casting is detected.
     */
    private fun detectScreenRecording(dm: DisplayManager): Boolean {
        // Check 1: Look for presentation displays (screen mirroring/casting)
        // Available since API 17, which is well within our minSdk 26
        val presentationDisplays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (presentationDisplays.isNotEmpty()) {
            Log.d(TAG, "Presentation display(s) found: ${presentationDisplays.size}")
            return true
        }

        // Check 2: Inspect all displays for virtual display flags
        // Virtual displays are created by screen recording apps and casting
        val allDisplays = dm.displays
        for (display in allDisplays) {
            // Skip the default display (display ID 0)
            if (display.displayId == Display.DEFAULT_DISPLAY) continue

            val flags = display.flags

            // FLAG_PRIVATE (1 << 2 = 4): Virtual display that is not mirroring the default display
            // These are often created by screen recording apps
            val isPrivate = (flags and Display.FLAG_PRIVATE) != 0

            // FLAG_PRESENTATION (1 << 1 = 2): Presentation display
            val isPresentation = (flags and Display.FLAG_PRESENTATION) != 0

            // A non-default display that is either private or presentation likely indicates
            // recording or mirroring
            if (isPrivate || isPresentation) {
                Log.d(TAG, "Virtual/presentation display detected: id=${display.displayId}, " +
                        "name=${display.name}, flags=$flags")
                return true
            }
        }

        return false
    }

    /**
     * Re-checks the current screen recording status. Called when press mode changes
     * or when the app needs to synchronize recording state (matching iOS's
     * recheckScreenRecordingStatus).
     */
    fun recheckScreenRecordingStatus() {
        val dm = displayManager
        if (dm != null) {
            checkRecordingState(dm)
        } else {
            Log.d(TAG, "Cannot recheck: recording detection not started")
        }
    }

    fun release() {
        stopRecordingDetection()
        currentWindow?.let { unregisterScreenCaptureCallback(it) }
        hideContentJob?.cancel()
        warningDismissJob?.cancel()
        currentWindow = null
        scope.cancel()
    }
}
