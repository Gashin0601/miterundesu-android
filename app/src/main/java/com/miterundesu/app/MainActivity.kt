package com.miterundesu.app

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.miterundesu.app.manager.CameraManager
import com.miterundesu.app.manager.ImageManager
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.PressModeManager
import com.miterundesu.app.manager.SecurityManager
import com.miterundesu.app.ui.theme.MiterundesuTheme

class MainActivity : ComponentActivity() {

    private lateinit var cameraManager: CameraManager
    private lateinit var imageManager: ImageManager
    private lateinit var securityManager: SecurityManager
    private lateinit var pressModeManager: PressModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Orientation lock: portrait only (matching iOS AppDelegate.orientationLock = .portrait)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val app = application as MiterundesuApplication

        cameraManager = CameraManager()
        imageManager = ImageManager(this)
        securityManager = SecurityManager()
        pressModeManager = PressModeManager(this, app.settingsManager, LocalizationManager)

        securityManager.enableSecurity(window)
        securityManager.startRecordingDetection(this)

        setContent {
            MiterundesuTheme(darkTheme = true) {
                MiterundesuAppContent(
                    cameraManager = cameraManager,
                    imageManager = imageManager,
                    securityManager = securityManager,
                    settingsManager = app.settingsManager,
                    pressModeManager = pressModeManager,
                    onboardingManager = app.onboardingManager,
                    networkMonitor = app.networkMonitor
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // On resume: remove expired images and restart camera if needed (matching iOS willEnterForeground)
        imageManager.removeExpiredImages()
        if (!cameraManager.isSessionRunning.value) {
            cameraManager.startSession()
        }
    }

    override fun onPause() {
        super.onPause()
        // On pause: clear sensitive data (matching iOS willResignActive)
        securityManager.clearSensitiveData()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
        securityManager.stopRecordingDetection()
        securityManager.release()
    }
}
