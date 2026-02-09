package com.miterundesu.app.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.ui.theme.MainGreen

/**
 * CompositionLocal for sharing spotlight target positions.
 * UI elements register their window-coordinate bounds via onGloballyPositioned + boundsInWindow().
 * SpotlightTutorialScreen reads these to draw accurate highlights (matching iOS GeometryReader approach).
 */
val LocalSpotlightFrames = compositionLocalOf<SnapshotStateMap<String, Rect>> {
    mutableStateMapOf()
}

data class TutorialStep(
    val titleKey: String,
    val descriptionKey: String,
    val targetIds: List<String>
)

/** Resolve individual target rects for a step, adjusted to canvas-local coords (matching iOS individual cutouts). */
private fun resolveTargetRects(
    step: TutorialStep,
    frames: Map<String, Rect>,
    canvasOffset: Offset
): List<Rect> {
    return step.targetIds.mapNotNull { frames[it] }
        .map { it.translate(-canvasOffset.x, -canvasOffset.y) }
}

/** Resolve the primary (first) target rect for card/arrow positioning. */
private fun resolvePrimaryTargetRect(
    step: TutorialStep,
    frames: Map<String, Rect>,
    canvasOffset: Offset
): Rect? {
    val first = step.targetIds.firstNotNullOfOrNull { frames[it] } ?: return null
    return first.translate(-canvasOffset.x, -canvasOffset.y)
}

@Composable
fun SpotlightTutorialScreen(
    onboardingManager: OnboardingManager,
    localizationManager: LocalizationManager,
    onComplete: () -> Unit
) {
    val frames = LocalSpotlightFrames.current
    val density = LocalDensity.current

    var currentStep by remember { mutableIntStateOf(0) }
    var canvasOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasHeight by remember { mutableStateOf(0f) }
    var canvasWidth by remember { mutableStateOf(0f) }

    // Tutorial steps matching iOS SpotlightTutorialView step definitions
    val steps = remember {
        listOf(
            TutorialStep(
                titleKey = "tutorial_step_zoom_title",
                descriptionKey = "tutorial_step_zoom_description",
                targetIds = listOf("zoom_buttons", "zoom_controls")
            ),
            TutorialStep(
                titleKey = "tutorial_step_capture_title",
                descriptionKey = "tutorial_step_capture_description",
                targetIds = listOf("shutter_button", "photo_button")
            ),
            TutorialStep(
                titleKey = "tutorial_step_theater_title",
                descriptionKey = "tutorial_step_theater_description",
                targetIds = listOf("theater_toggle")
            ),
            TutorialStep(
                titleKey = "tutorial_step_message_title",
                descriptionKey = "tutorial_step_message_description",
                targetIds = listOf("scrolling_message", "explanation_button")
            ),
            TutorialStep(
                titleKey = "tutorial_step_settings_title",
                descriptionKey = "tutorial_step_settings_description",
                targetIds = listOf("settings_button")
            )
        )
    }

    val totalSteps = steps.size
    val step = steps[currentStep]
    val targetRects = resolveTargetRects(step, frames, canvasOffset)
    val targetRect = resolvePrimaryTargetRect(step, frames, canvasOffset)

    // Announce step change for TalkBack
    val view = LocalView.current
    LaunchedEffect(currentStep) {
        view.announceForAccessibility(
            "${localizationManager.localizedString(step.titleKey)}, ${currentStep + 1} / $totalSteps"
        )
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "spotlight_alpha"
    )

    // Card dimensions for position calculation
    val cardWidthPx = with(density) { 300.dp.toPx() }
    val cardHeightPx = with(density) { 220.dp.toPx() }

    Box(modifier = Modifier.fillMaxSize()) {
        // Spotlight overlay canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    canvasOffset = coords.positionInWindow()
                    canvasWidth = coords.size.width.toFloat()
                    canvasHeight = coords.size.height.toFloat()
                }
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .clearAndSetSemantics { }
        ) {
            // Draw semi-transparent overlay (0.7 opacity to match iOS)
            drawRect(
                color = Color.Black.copy(alpha = 0.7f * animatedAlpha),
                size = size
            )

            // Cut out individual spotlight areas (matching iOS SpotlightOverlay which draws
            // separate rounded rect cutouts for each target frame)
            if (targetRects.isNotEmpty()) {
                val padding = 8.dp.toPx()
                val cornerRadiusPx = 12.dp.toPx()

                for (rect in targetRects) {
                    val expandedRect = Rect(
                        offset = Offset(
                            rect.left - padding,
                            rect.top - padding
                        ),
                        size = Size(
                            rect.width + padding * 2,
                            rect.height + padding * 2
                        )
                    )
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = expandedRect,
                                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                            )
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color.Black,
                        blendMode = BlendMode.DstOut
                    )
                }
            }
        }

        if (targetRect != null && canvasHeight > 0f) {
            // Calculate card position
            val targetCenterY = targetRect.center.y
            val isTargetInTopHalf = targetCenterY < canvasHeight / 2f
            val cardOffsetYPx = if (isTargetInTopHalf) {
                (targetRect.bottom + 40f).coerceAtMost(canvasHeight * 0.6f)
            } else {
                (targetRect.top - cardHeightPx - 40f).coerceAtLeast(canvasHeight * 0.05f)
            }

            val cardCenterXPx = canvasWidth / 2f
            val targetCenter = targetRect.center

            // Dashed arrow from card to target (matching iOS dash: [8, 4], lineWidth 3)
            val arrowStartY: Float
            val arrowEndY: Float
            if (isTargetInTopHalf) {
                arrowStartY = cardOffsetYPx
                arrowEndY = targetRect.bottom + with(density) { 8.dp.toPx() }
            } else {
                arrowStartY = cardOffsetYPx + cardHeightPx
                arrowEndY = targetRect.top - with(density) { 8.dp.toPx() }
            }

            // Arrow with shadow and dashed line (matching iOS StrokeStyle dash: [8, 4])
            Canvas(modifier = Modifier
                .fillMaxSize()
                .clearAndSetSemantics { }
            ) {
                val dashOn = 8.dp.toPx()
                val dashOff = 4.dp.toPx()
                val strokeW = 3.dp.toPx()

                // Shadow layer (offset y+1, matching iOS shadow radius 2, y: 1)
                drawLine(
                    color = Color.Black.copy(alpha = 0.3f),
                    start = Offset(cardCenterXPx, arrowStartY + 1.dp.toPx()),
                    end = Offset(targetCenter.x, arrowEndY + 1.dp.toPx()),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashOn, dashOff),
                        0f
                    )
                )

                // Main dashed white line
                drawLine(
                    color = Color.White,
                    start = Offset(cardCenterXPx, arrowStartY),
                    end = Offset(targetCenter.x, arrowEndY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(dashOn, dashOff),
                        0f
                    )
                )
            }

            val cardOffsetYDp = with(density) { cardOffsetYPx.toDp() }

            // Tutorial description card with step indicator and nav buttons inside
            TutorialDescriptionCard(
                title = localizationManager.localizedString(step.titleKey),
                description = localizationManager.localizedString(step.descriptionKey),
                currentIndex = currentStep,
                totalSteps = totalSteps,
                localizationManager = localizationManager,
                onPrevious = if (currentStep > 0) {
                    { currentStep-- }
                } else null,
                onNext = if (currentStep < totalSteps - 1) {
                    { currentStep++ }
                } else null,
                onComplete = {
                    onboardingManager.completeFeatureHighlights()
                    onComplete()
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = cardOffsetYDp)
            )
        }
    }
}

@Composable
private fun TutorialDescriptionCard(
    title: String,
    description: String,
    currentIndex: Int,
    totalSteps: Int,
    localizationManager: LocalizationManager,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(300.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                MainGreen,
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
            .semantics(mergeDescendants = true) { },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step indicator dots (inside card, uniform 8dp, inactive 0.3 opacity)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clearAndSetSemantics { }
        ) {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentIndex) {
                                Color.White
                            } else {
                                Color.White.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation buttons (inside card, green text on white background with chevron icons)
        Row(
            modifier = Modifier.width(260.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous button
            if (onPrevious != null) {
                Button(
                    onClick = onPrevious,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MainGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clearAndSetSemantics { }
                        )
                        Text(
                            text = localizationManager.localizedString("tutorial_back"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(80.dp))
            }

            // Next / Complete button
            if (onNext != null) {
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MainGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("tutorial_next"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clearAndSetSemantics { }
                        )
                    }
                }
            } else {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MainGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("tutorial_complete"),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                                .clearAndSetSemantics { }
                        )
                    }
                }
            }
        }
    }
}
