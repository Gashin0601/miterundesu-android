package com.miterundesu.app.ui.component

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.camera.view.PreviewView
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
    ) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        val newZoom = currentZoom * zoom
                        cameraManager.zoom(newZoom)
                    }
                },
            update = { view ->
                if (hideContent) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        view.setRenderEffect(
                            RenderEffect.createBlurEffect(
                                100f, 100f, Shader.TileMode.CLAMP
                            )
                        )
                    }
                } else if (isRecording) {
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

        WatermarkView(
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}
