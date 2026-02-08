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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miterundesu.app.manager.LocalizationManager
import com.miterundesu.app.manager.OnboardingManager
import com.miterundesu.app.ui.theme.MainGreen
import kotlin.math.roundToInt

data class TutorialStep(
    val titleKey: String,
    val descriptionKey: String,
    val targetRect: Rect,
    val isCircle: Boolean = false
)

@Composable
fun SpotlightTutorialScreen(
    onboardingManager: OnboardingManager,
    localizationManager: LocalizationManager,
    onComplete: () -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    var currentStep by remember { mutableIntStateOf(0) }

    // Define tutorial steps with target positions.
    // These are reasonable positions based on typical layout geometry.
    val steps = remember(screenWidthPx, screenHeightPx) {
        val centerX = screenWidthPx / 2f
        listOf(
            // Step 1: Zoom controls - bottom center of camera area
            TutorialStep(
                titleKey = "tutorial_step_zoom_title",
                descriptionKey = "tutorial_step_zoom_description",
                targetRect = Rect(
                    offset = Offset(centerX - 80f, screenHeightPx * 0.55f),
                    size = Size(160f, 48f)
                ),
                isCircle = false
            ),
            // Step 2: Capture / shutter button - bottom center
            TutorialStep(
                titleKey = "tutorial_step_capture_title",
                descriptionKey = "tutorial_step_capture_description",
                targetRect = Rect(
                    offset = Offset(centerX - 36f, screenHeightPx * 0.82f),
                    size = Size(72f, 72f)
                ),
                isCircle = true
            ),
            // Step 3: Theater mode toggle - top left
            TutorialStep(
                titleKey = "tutorial_step_theater_title",
                descriptionKey = "tutorial_step_theater_description",
                targetRect = Rect(
                    offset = Offset(16f, screenHeightPx * 0.06f),
                    size = Size(44f, 44f)
                ),
                isCircle = true
            ),
            // Step 4: Scrolling message - top area
            TutorialStep(
                titleKey = "tutorial_step_message_title",
                descriptionKey = "tutorial_step_message_description",
                targetRect = Rect(
                    offset = Offset(16f, screenHeightPx * 0.12f),
                    size = Size(screenWidthPx - 32f, 36f)
                ),
                isCircle = false
            ),
            // Step 5: Settings button - top right
            TutorialStep(
                titleKey = "tutorial_step_settings_title",
                descriptionKey = "tutorial_step_settings_description",
                targetRect = Rect(
                    offset = Offset(screenWidthPx - 60f, screenHeightPx * 0.06f),
                    size = Size(44f, 44f)
                ),
                isCircle = true
            )
        )
    }

    val totalSteps = steps.size
    val step = steps[currentStep]

    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "spotlight_alpha"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Spotlight overlay
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        ) {
            // Draw semi-transparent overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.75f * animatedAlpha),
                size = size
            )

            // Cut out spotlight area
            val padding = 12f
            val targetRect = step.targetRect
            val expandedRect = Rect(
                offset = Offset(
                    targetRect.left - padding,
                    targetRect.top - padding
                ),
                size = Size(
                    targetRect.width + padding * 2,
                    targetRect.height + padding * 2
                )
            )

            if (step.isCircle) {
                val radius = maxOf(expandedRect.width, expandedRect.height) / 2f
                drawCircle(
                    color = Color.Black,
                    radius = radius,
                    center = expandedRect.center,
                    blendMode = BlendMode.DstOut
                )
            } else {
                val path = Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = expandedRect,
                            cornerRadius = CornerRadius(16f, 16f)
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

        // Tutorial description card
        val targetCenterY = step.targetRect.center.y
        val isTargetInTopHalf = targetCenterY < screenHeightPx / 2f
        val cardOffsetYPx = if (isTargetInTopHalf) {
            (step.targetRect.bottom + 40f).coerceAtMost(screenHeightPx * 0.6f)
        } else {
            (step.targetRect.top - 220f).coerceAtLeast(screenHeightPx * 0.05f)
        }

        val cardOffsetYDp = with(density) { cardOffsetYPx.toDp() }

        TutorialDescriptionCard(
            title = localizationManager.localizedString(step.titleKey),
            description = localizationManager.localizedString(step.descriptionKey),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = cardOffsetYDp)
        )

        // Arrow from card to target
        val cardCenterXPx = screenWidthPx / 2f
        val cardTopPx = cardOffsetYPx
        val cardBottomPx = cardOffsetYPx + with(density) { 160.dp.toPx() }
        val targetCenter = step.targetRect.center

        val arrowStartY: Float
        val arrowEndY: Float
        if (isTargetInTopHalf) {
            arrowStartY = cardTopPx
            arrowEndY = step.targetRect.bottom + 12f
        } else {
            arrowStartY = cardBottomPx
            arrowEndY = step.targetRect.top - 12f
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(cardCenterXPx, arrowStartY),
                end = Offset(targetCenter.x, arrowEndY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(
                    intervals = floatArrayOf(12f, 8f),
                    phase = 0f
                )
            )
        }

        // Bottom navigation
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step indicator dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalSteps) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == currentStep) 10.dp else 8.dp)
                            .background(
                                color = if (index == currentStep) {
                                    Color.White
                                } else {
                                    Color.White.copy(alpha = 0.4f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous button
                if (currentStep > 0) {
                    TextButton(
                        onClick = { currentStep-- }
                    ) {
                        Text(
                            text = localizationManager.localizedString("tutorial_previous"),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                // Next / Done button
                if (currentStep < totalSteps - 1) {
                    Button(
                        onClick = { currentStep++ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MainGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("tutorial_next"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            onboardingManager.completeFeatureHighlights()
                            onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = MainGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = localizationManager.localizedString("tutorial_done"),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorialDescriptionCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                Color(0xFF2C2C2E),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Start
        )
    }
}
