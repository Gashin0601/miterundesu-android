package com.miterundesu.app

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import com.miterundesu.app.ui.screen.LocalSpotlightFrames
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
import android.app.Activity

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
    val isCameraReady by cameraManager.isCameraReady.collectAsState()
    val hideContent by securityManager.hideContent.collectAsState()
    val isRecording by securityManager.isRecording.collectAsState()
    val showScreenshotWarning by securityManager.showScreenshotWarning.collectAsState()
    val showRecordingWarning by securityManager.showRecordingWarning.collectAsState()
    val isLoggedIn by pressModeManager.isLoggedIn.collectAsState()
    val pressAccount by pressModeManager.pressAccount.collectAsState()
    val isConnected by networkMonitor.isConnected.collectAsState()
    val shouldShowWhatsNew by app.whatsNewManager.shouldShowWhatsNew.collectAsState()
    val showWelcomeScreen by onboardingManager.showWelcomeScreen.collectAsState()
    val showFeatureHighlights by onboardingManager.showFeatureHighlights.collectAsState()
    val showCompletionScreen by onboardingManager.showCompletionScreen.collectAsState()
    val hasCompletedOnboarding by onboardingManager.hasCompletedOnboarding.collectAsState()
    val isPressModeEnabled by pressModeManager.isPressModeEnabled.collectAsState()

    val scrollingMessage = if (isTheaterMode) scrollingMessageTheater else scrollingMessageNormal

    // Shared spotlight frames for tutorial overlay (matching iOS GeometryReader/PreferenceKey pattern)
    val spotlightFrames = remember { mutableStateMapOf<String, Rect>() }

    LaunchedEffect(maxZoomFactor) {
        cameraManager.maxZoom = maxZoomFactor
    }

    // Onboarding auto-check: on first composition, check onboarding status (matching iOS .onAppear)
    LaunchedEffect(Unit) {
        onboardingManager.checkOnboardingStatus()
        securityManager.recheckScreenRecordingStatus()
    }

    // Issue 1: Sync press mode with SecurityManager (matching iOS ContentView .onChange(of: isPressMode))
    LaunchedEffect(isPressModeEnabled) {
        val activity = context as? Activity
        val window = activity?.window
        securityManager.isPressModeEnabled = isPressModeEnabled
        if (isPressModeEnabled) {
            window?.let { securityManager.disableSecurity(it) }
        } else {
            window?.let { securityManager.enableSecurity(it) }
            securityManager.recheckScreenRecordingStatus()
        }
    }

    // Issue 3: Screenshot detection dismisses image preview (matching iOS .onChange(of: hideContent))
    LaunchedEffect(hideContent) {
        if (hideContent) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == AppRoutes.IMAGE_GALLERY || currentRoute == AppRoutes.CAPTURED_PREVIEW) {
                navController.popBackStack(AppRoutes.MAIN, inclusive = false)
            }
        }
    }

    // WhatsNew auto-show: navigate when shouldShowWhatsNew becomes true (matching iOS .fullScreenCover)
    LaunchedEffect(shouldShowWhatsNew) {
        if (shouldShowWhatsNew) {
            navController.navigate(AppRoutes.WHATS_NEW)
        }
    }

    // Onboarding auto-navigate: when showWelcomeScreen becomes true (matching iOS .fullScreenCover)
    LaunchedEffect(showWelcomeScreen) {
        if (showWelcomeScreen) {
            navController.navigate(AppRoutes.TUTORIAL_WELCOME) {
                launchSingleTop = true
            }
        }
    }

    // When spotlight tutorial starts, pop back to MAIN so it's visible behind the overlay
    LaunchedEffect(showFeatureHighlights) {
        if (showFeatureHighlights) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute != AppRoutes.MAIN) {
                navController.popBackStack(AppRoutes.MAIN, inclusive = false)
            }
        }
    }

    // Auto-navigate to completion screen when showCompletionScreen becomes true
    LaunchedEffect(showCompletionScreen) {
        if (showCompletionScreen) {
            navController.navigate(AppRoutes.TUTORIAL_COMPLETION) {
                launchSingleTop = true
            }
        }
    }

    // Auto-navigate back to main when onboarding completes (skip/completion button)
    // Only navigate if we're currently on an onboarding screen
    // Note: This handles first-time onboarding. For re-show tutorial, the callbacks
    // in TutorialCompletionScreen.onFinish and TutorialWelcomeScreen.onSkip handle navigation
    // directly, since hasCompletedOnboarding won't change (already true).
    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            if (currentRoute == AppRoutes.TUTORIAL_WELCOME ||
                currentRoute == AppRoutes.TUTORIAL_COMPLETION) {
                navController.popBackStack(AppRoutes.MAIN, inclusive = false)
            }
        }
    }

    val fullScreenEnter = slideInVertically(initialOffsetY = { it }) + fadeIn()
    val fullScreenExit = slideOutVertically(targetOffsetY = { it }) + fadeOut()

    CompositionLocalProvider(LocalSpotlightFrames provides spotlightFrames) {
    Box(modifier = Modifier.fillMaxSize()) {
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
                isCameraReady = isCameraReady,
                hideContent = hideContent,
                isRecording = isRecording,
                isPressModeEnabled = isPressModeEnabled,
                showScreenshotWarning = showScreenshotWarning,
                showRecordingWarning = showRecordingWarning,
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
                        imageData?.let {
                            imageManager.addImage(it)
                            navController.navigate(AppRoutes.CAPTURED_PREVIEW)
                        }
                    }
                },
                onScreenTap = {},
                onDismissScreenshotWarning = {
                    securityManager.dismissScreenshotWarning()
                },
                localizationManager = LocalizationManager,
                settingsManager = settingsManager,
                cameraPreview = { mod ->
                    BoxWithConstraints(modifier = mod) {
                        val cameraScreenWidth = maxWidth
                        val buttonPadding = cameraScreenWidth * 0.03f

                        CameraPreview(
                            cameraManager = cameraManager,
                            securityManager = securityManager,
                            modifier = Modifier.fillMaxSize()
                        )
                        ZoomControls(
                            currentZoom = zoomFactor,
                            maxZoom = maxZoomFactor,
                            onZoomChange = { cameraManager.zoom(it) },
                            screenWidth = cameraScreenWidth,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = buttonPadding, bottom = buttonPadding)
                                .onGloballyPositioned {
                                    spotlightFrames["zoom_buttons"] = it.boundsInWindow()
                                },
                            onSmoothZoomChange = { target, duration ->
                                cameraManager.smoothZoom(target, duration)
                            }
                        )
                    }
                },
                shutterButton = { shutterSize ->
                    ShutterButton(
                        isCapturing = isCapturing,
                        isTheaterMode = isTheaterMode,
                        onClick = {
                            cameraManager.capturePhoto { imageData ->
                                imageData?.let {
                                    imageManager.addImage(it)
                                    navController.navigate(AppRoutes.CAPTURED_PREVIEW)
                                }
                            }
                        },
                        buttonSize = shutterSize
                    )
                }
            )
        }

        composable(AppRoutes.SETTINGS) {
            SettingsScreen(
                localizationManager = LocalizationManager,
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
                localizationManager = LocalizationManager,
                isTheaterMode = isTheaterMode,
                onToggleTheaterMode = { settingsManager.setTheaterMode(!isTheaterMode) },
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
            val latestImage = images.firstOrNull()
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
                onStartTutorial = { /* Navigation handled by state observation */ },
                onSkip = { navController.popBackStack(AppRoutes.MAIN, inclusive = false) }
            )
        }

        composable(AppRoutes.TUTORIAL_COMPLETION) {
            TutorialCompletionScreen(
                onboardingManager = onboardingManager,
                localizationManager = LocalizationManager,
                onFinish = { navController.popBackStack(AppRoutes.MAIN, inclusive = false) }
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

    // Spotlight tutorial overlay on top of NavHost (matching iOS overlay approach)
    // This keeps MainScreen visible behind the semi-transparent dark overlay
    if (showFeatureHighlights) {
        SpotlightTutorialScreen(
            onboardingManager = onboardingManager,
            localizationManager = LocalizationManager,
            onComplete = { /* Navigation handled by state observation */ }
        )
    }
    } // Box
    } // CompositionLocalProvider
}

private fun getVersionName(context: Context): String {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        pInfo.versionName ?: "1.1.0"
    } catch (_: Exception) {
        "1.1.0"
    }
}
