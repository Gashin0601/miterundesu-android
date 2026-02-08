package com.miterundesu.app.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.Settings
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.miterundesu.app.util.withWatermark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.max

class CameraManager {

    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var context: Context? = null

    private val _currentZoomFactor = MutableStateFlow(1f)
    val currentZoomFactor: StateFlow<Float> = _currentZoomFactor.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    var maxZoom: Float = 100f

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        this.context = context
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (_: Exception) {
                // Camera initialization failed
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun zoom(factor: Float) {
        val clamped = factor.coerceIn(1f, maxZoom)
        camera?.cameraControl?.setZoomRatio(clamped)
        _currentZoomFactor.value = clamped
    }

    fun smoothZoom(to: Float, duration: Float) {
        val target = to.coerceIn(1f, maxZoom)
        val start = _currentZoomFactor.value
        val steps = max(1, (duration / 0.016f).toInt())
        scope.launch {
            for (i in 1..steps) {
                val fraction = i.toFloat() / steps
                val eased = fraction * fraction * (3f - 2f * fraction) // smoothstep
                val current = start + (target - start) * eased
                zoom(current)
                delay(16L)
            }
            zoom(target)
        }
    }

    fun capturePhoto(onCaptured: (ByteArray) -> Unit) {
        if (_isCapturing.value) return
        val capture = imageCapture ?: return
        val ctx = context ?: return

        _isCapturing.value = true

        capture.takePicture(
            ContextCompat.getMainExecutor(ctx),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    scope.launch(Dispatchers.Default) {
                        try {
                            val bytes = imageProxyToByteArray(image)
                            image.close()

                            val bitmap = downsampleBitmap(bytes, 2048)
                            if (bitmap != null) {
                                val watermarkText = generateWatermarkText(ctx)
                                val watermarked = bitmap.withWatermark(watermarkText)

                                val output = ByteArrayOutputStream()
                                watermarked.compress(Bitmap.CompressFormat.JPEG, 60, output)
                                val result = output.toByteArray()

                                if (watermarked !== bitmap) {
                                    watermarked.recycle()
                                }
                                bitmap.recycle()

                                scope.launch(Dispatchers.Main) {
                                    _isCapturing.value = false
                                    onCaptured(result)
                                }
                            } else {
                                scope.launch(Dispatchers.Main) {
                                    _isCapturing.value = false
                                }
                            }
                        } catch (_: Exception) {
                            scope.launch(Dispatchers.Main) {
                                _isCapturing.value = false
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    _isCapturing.value = false
                }
            }
        )
    }

    private fun imageProxyToByteArray(image: ImageProxy): ByteArray {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return bytes
    }

    private fun downsampleBitmap(data: ByteArray, maxDimension: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1

        if (width > maxDimension || height > maxDimension) {
            val halfWidth = width / 2
            val halfHeight = height / 2
            while ((halfWidth / inSampleSize) >= maxDimension ||
                (halfHeight / inSampleSize) >= maxDimension
            ) {
                inSampleSize *= 2
            }
        }

        val decodeOptions = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, decodeOptions)
            ?: return null

        val currentMax = max(bitmap.width, bitmap.height)
        if (currentMax <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / currentMax
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
        if (scaled !== bitmap) {
            bitmap.recycle()
        }
        return scaled
    }

    private fun generateWatermarkText(context: Context): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
        val dateStr = LocalDateTime.now().format(formatter)
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )?.take(6) ?: "000000"
        return "$dateStr | ID: $deviceId"
    }

    fun release() {
        cameraProvider?.unbindAll()
        camera = null
        imageCapture = null
        cameraProvider = null
        context = null
        scope.cancel()
    }
}
