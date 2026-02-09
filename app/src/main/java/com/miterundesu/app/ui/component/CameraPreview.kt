package com.miterundesu.app.ui.component

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.miterundesu.app.manager.CameraManager
import com.miterundesu.app.manager.SecurityManager

@Composable
fun CameraPreview(
    cameraManager: CameraManager,
    securityManager: SecurityManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentZoom by cameraManager.currentZoomFactor.collectAsStateWithLifecycle()
    val isRecording by securityManager.isRecording.collectAsStateWithLifecycle()
    val hideContent by securityManager.hideContent.collectAsStateWithLifecycle()

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    DisposableEffect(lifecycleOwner) {
        cameraManager.initialize(context, lifecycleOwner, previewView)
        onDispose {
            // Camera will be released by the manager
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
    ) {
        val screenWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val cornerRadiusDp = with(LocalDensity.current) { (screenWidthPx * 0.05f).toDp() }
        val watermarkPaddingDp = with(LocalDensity.current) { (screenWidthPx * 0.04f).toDp() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(cornerRadiusDp))
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clearAndSetSemantics { } // Camera preview is decorative for TalkBack
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            val newZoom = currentZoom * zoom
                            cameraManager.zoom(newZoom)
                        }
                    },
                update = { view ->
                    if (isRecording && !hideContent) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            view.setRenderEffect(
                                RenderEffect.createBlurEffect(
                                    30f, 30f, Shader.TileMode.CLAMP
                                )
                            )
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            view.setRenderEffect(null)
                        }
                    }
                }
            )

            // Issue 4: Show solid black overlay when hideContent is true (matching iOS Color.black)
            if (hideContent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }

            WatermarkView(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = watermarkPaddingDp, bottom = watermarkPaddingDp)
                    .clearAndSetSemantics { }
            )
        }
    }
}
