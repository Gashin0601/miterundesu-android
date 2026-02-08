package com.miterundesu.app

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miterundesu.app.manager.CameraManager
import com.miterundesu.app.manager.ImageManager
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.NetworkMonitor
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.manager.PressModeManager
import com.miterundesu.app.manager.SecurityManager
import com.miterundesu.app.manager.SettingsManager
import com.miterundesu.app.ui.component.CameraPreview
import com.miterundesu.app.ui.component.ShutterButton
import com.miterundesu.app.ui.component.ZoomControls
import com.miterundesu.app.ui.screen.CapturedImagePreviewScreen
import com.miterundesu.app.ui.screen.ExplanationScreen
import com.miterundesu.app.ui.screen.ImageGalleryScreen
import com.miterundesu.app.ui.screen.MainScreen
import com.miterundesu.app.ui.screen.PressModeAccountStatusScreen
import com.miterundesu.app.ui.screen.PressModeInfoScreen
import com.miterundesu.app.ui.screen.PressModeLoginScreen
import com.miterundesu.app.ui.screen.SettingsScreen
import com.miterundesu.app.ui.screen.SpotlightTutorialScreen
import com.miterundesu.app.ui.screen.TutorialCompletionScreen
import com.miterundesu.app.ui.screen.TutorialWelcomeScreen
import com.miterundesu.app.ui.screen.WhatsNewScreen

object AppRoutes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val EXPLANATION = "explanation"
    const val IMAGE_GALLERY = "imageGallery"
    const val CAPTURED_PREVIEW = "capturedPreview"
    const val PRESS_MODE_LOGIN = "pressModeLogin"
    const val PRESS_MODE_INFO = "pressModeInfo"
    const val PRESS_MODE_ACCOUNT_STATUS = "pressModeAccountStatus"
    const val TUTORIAL_WELCOME = "tutorialWelcome"
    const val SPOTLIGHT_TUTORIAL = "spotlightTutorial"
    const val TUTORIAL_COMPLETION = "tutorialCompletion"
    const val WHATS_NEW = "whatsNew"
}

@Composable
fun MiterundesuAppContent(
    cameraManager: CameraManager,
    imageManager: ImageManager,
    securityManager: SecurityManager,
    settingsManager: SettingsManager,
    pressModeManager: PressModeManager,
    onboardingManager: OnboardingManager,
    networkMonitor: NetworkMonitor,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as MiterundesuApplication

    val isTheaterMode by settingsManager.isTheaterMode.collectAsState()
    val maxZoomFactor by settingsManager.maxZoomFactor.collectAsState()
    val language by settingsManager.language.collectAsState()
    val scrollingMessageNormal by settingsManager.scrollingMessageNormal.collectAsState()
    val scrollingMessageTheater by settingsManager.scrollingMessageTheater.collectAsState()
    val images by imageManager.images.collectAsState()
    val zoomFactor by cameraManager.currentZoomFactor.collectAsState()
    val isCapturing by cameraManager.isCapturing.collectAsState()
    val hideContent by securityManager.hideContent.collectAsState()
    val isLoggedIn by pressModeManager.isLoggedIn.collectAsState()
    val pressAccount by pressModeManager.pressAccount.collectAsState()
    val isConnected by networkMonitor.isConnected.collectAsState()

    val scrollingMessage = if (isTheaterMode) scrollingMessageTheater else scrollingMessageNormal

    LaunchedEffect(maxZoomFactor) {
        cameraManager.maxZoom = maxZoomFactor
    }

    val fullScreenEnter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    val fullScreenExit = slideOutVertically(targetOffsetY = { it }) + fadeOut()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.MAIN,
        enterTransition = { fullScreenEnter },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fullScreenExit }
    ) {
        composable(
            AppRoutes.MAIN,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            MainScreen(
                isTheaterMode = isTheaterMode,
                scrollingMessage = scrollingMessage,
                images = images,
                zoomFactor = zoomFactor,
                isCapturing = isCapturing,
                hideContent = hideContent,
                onTheaterToggle = {
                    settingsManager.setTheaterMode(!isTheaterMode)
                },
                onExplanationClick = {
                    navController.navigate(AppRoutes.EXPLANATION)
                },
                onSettingsClick = {
                    navController.navigate(AppRoutes.SETTINGS)
                },
                onGalleryClick = {
                    if (images.isNotEmpty()) {
                        navController.navigate(AppRoutes.IMAGE_GALLERY)
                    }
                },
                onCaptureClick = {
                    cameraManager.capturePhoto { imageData ->
                        imageManager.addImage(imageData)
                        navController.navigate(AppRoutes.CAPTURED_PREVIEW)
                    }
                },
                onScreenTap = {},
                cameraPreview = { mod ->
                    Box(modifier = mod) {
                        CameraPreview(
                            cameraManager = cameraManager,
                            securityManager = securityManager,
                            modifier = Modifier.fillMaxSize()
                        )
                        ZoomControls(
                            currentZoom = zoomFactor,
                            maxZoom = maxZoomFactor,
                            onZoomChange = { cameraManager.zoom(it) },
                            onSmoothZoom = { target, duration ->
                                cameraManager.smoothZoom(target, duration)
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                },
                shutterButton = {
                    ShutterButton(
                        isCapturing = isCapturing,
                        onClick = {
                            cameraManager.capturePhoto { imageData ->
                                imageManager.addImage(imageData)
                                navController.navigate(AppRoutes.CAPTURED_PREVIEW)
                            }
                        }
                    )
                }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                isTheaterMode = isTheaterMode,
                maxZoomFactor = maxZoomFactor,
                language = language,
                scrollingMessageNormal = scrollingMessageNormal,
                scrollingMessageTheater = scrollingMessageTheater,
                isPressMode = pressModeManager.isPressModeEnabled.collectAsState().value,
                isLoggedIn = isLoggedIn,
                pressAccountSummary = pressAccount?.summary,
                isConnected = isConnected,
                versionName = getVersionName(context),
                onBack = { navController.popBackStack() },
                onTheaterToggle = {
                    settingsManager.setTheaterMode(!isTheaterMode)
                },
                onZoomChange = { settingsManager.setMaxZoomFactor(it) },
                onLanguageChange = { settingsManager.setLanguage(it) },
                onScrollingMessageNormalChange = { settingsManager.setScrollingMessageNormal(it) },
                onScrollingMessageTheaterChange = { settingsManager.setScrollingMessageTheater(it) },
                onPressModeLoginClick = {
                    navController.navigate(AppRoutes.PRESS_MODE_LOGIN)
                },
                onPressModeInfoClick = {
                    navController.navigate(AppRoutes.PRESS_MODE_INFO)
                },
                onPressModeAccountStatusClick = {
                    navController.navigate(AppRoutes.PRESS_MODE_ACCOUNT_STATUS)
                },
                onLogout = { pressModeManager.logout() },
                onShowTutorial = {
                    onboardingManager.showTutorial()
                    navController.popBackStack()
                    navController.navigate(AppRoutes.TUTORIAL_WELCOME)
                },
                onResetSettings = { settingsManager.resetToDefaults() }
            )
        }

        composable(AppRoutes.EXPLANATION) {
            ExplanationScreen(
                isTheaterMode = isTheaterMode,
                onClose = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.IMAGE_GALLERY) {
            ImageGalleryScreen(
                imageManager = imageManager,
                securityManager = securityManager,
                settingsManager = settingsManager,
                pressModeManager = pressModeManager,
                localizationManager = LocalizationManager,
                onClose = { navController.popBackStack() },
                onExplanation = { navController.navigate(AppRoutes.EXPLANATION) },
                onImageDeleted = { }
            )
        }

        composable(AppRoutes.CAPTURED_PREVIEW) {
            val latestImage = images.lastOrNull()
            if (latestImage != null) {
                CapturedImagePreviewScreen(
                    image = latestImage,
                    securityManager = securityManager,
                    settingsManager = settingsManager,
                    pressModeManager = pressModeManager,
                    localizationManager = LocalizationManager,
                    onClose = { navController.popBackStack() },
                    onExplanation = { navController.navigate(AppRoutes.EXPLANATION) },
                    onSettings = { navController.navigate(AppRoutes.SETTINGS) }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable(AppRoutes.PRESS_MODE_LOGIN) {
            PressModeLoginScreen(
                pressModeManager = pressModeManager,
                localizationManager = LocalizationManager,
                onClose = { navController.popBackStack() },
                onLoginSuccess = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PRESS_MODE_INFO) {
            PressModeInfoScreen(
                localizationManager = LocalizationManager,
                onClose = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.PRESS_MODE_ACCOUNT_STATUS) {
            PressModeAccountStatusScreen(
                pressModeManager = pressModeManager,
                localizationManager = LocalizationManager,
                onClose = { navController.popBackStack() }
            )
        }

        composable(AppRoutes.TUTORIAL_WELCOME) {
            TutorialWelcomeScreen(
                onboardingManager = onboardingManager,
                localizationManager = LocalizationManager,
                onStartTutorial = {
                    navController.navigate(AppRoutes.SPOTLIGHT_TUTORIAL) {
                        popUpTo(AppRoutes.TUTORIAL_WELCOME) { inclusive = true }
                    }
                },
                onSkip = {
                    onboardingManager.completeOnboarding()
                    navController.popBackStack(AppRoutes.MAIN, inclusive = false)
                }
            )
        }

        composable(AppRoutes.SPOTLIGHT_TUTORIAL) {
            SpotlightTutorialScreen(
                onboardingManager = onboardingManager,
                localizationManager = LocalizationManager,
                onComplete = {
                    navController.navigate(AppRoutes.TUTORIAL_COMPLETION) {
                        popUpTo(AppRoutes.SPOTLIGHT_TUTORIAL) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoutes.TUTORIAL_COMPLETION) {
            TutorialCompletionScreen(
                onboardingManager = onboardingManager,
                localizationManager = LocalizationManager,
                onFinish = {
                    onboardingManager.completeOnboarding()
                    navController.popBackStack(AppRoutes.MAIN, inclusive = false)
                }
            )
        }

        composable(AppRoutes.WHATS_NEW) {
            WhatsNewScreen(
                whatsNewManager = app.whatsNewManager,
                localizationManager = LocalizationManager,
                onDismiss = { navController.popBackStack() }
            )
        }
    }
}

private fun getVersionName(context: Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.1.0"
    } catch (_: Exception) {
        "1.1.0"
    }
}
