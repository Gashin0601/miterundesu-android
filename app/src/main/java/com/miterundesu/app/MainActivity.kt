package com.miterundesu.app

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

        val app = application as MiterundesuApplication

        cameraManager = CameraManager()
        imageManager = ImageManager(this)
        securityManager = SecurityManager()
        pressModeManager = PressModeManager(this, app.settingsManager, LocalizationManager)

        securityManager.enableSecurity(window)

        setContent {
            MiterundesuTheme {
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

    override fun onDestroy() {
        super.onDestroy()
        cameraManager.release()
        securityManager.release()
    }
}
