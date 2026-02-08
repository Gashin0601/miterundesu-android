package com.miterundesu.app.manager

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.miterundesu.app.data.local.AppDatabase
import com.miterundesu.app.data.local.CapturedImageEntity
import com.miterundesu.app.data.model.CapturedImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.LinkedHashMap
import java.util.UUID

class ImageManager(context: Context) : ComponentCallbacks2 {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.capturedImageDao()
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _images = MutableStateFlow<List<CapturedImage>>(emptyList())
    val images: StateFlow<List<CapturedImage>> = _images.asStateFlow()

    private val timerJobs = mutableMapOf<UUID, Job>()
    private val cacheMutex = Mutex()
    private val bitmapCache = object : LinkedHashMap<UUID, Bitmap>(2, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<UUID, Bitmap>?): Boolean {
            if (size > MAX_CACHE_SIZE) {
                eldest?.value?.recycle()
                return true
            }
            return false
        }
    }

    init {
        context.registerComponentCallbacks(this)
        loadImages()
    }

    private fun loadImages() {
        scope.launch {
            val entities = withContext(Dispatchers.IO) {
                dao.getAll()
            }
            val now = Instant.now()
            val loaded = entities.mapNotNull { entity ->
                val image = entity.toCapturedImage()
                if (image.expiresAt.isAfter(now)) {
                    image
                } else {
                    withContext(Dispatchers.IO) {
                        dao.deleteById(entity.id)
                    }
                    null
                }
            }
            _images.value = loaded
            loaded.forEach { image ->
                startTimer(image)
            }
        }
    }

    fun addImage(imageData: ByteArray) {
        scope.launch {
            val now = Instant.now()
            val image = CapturedImage(
                id = UUID.randomUUID(),
                capturedAt = now,
                expiresAt = now.plusSeconds(600),
                imageData = imageData
            )
            val entity = CapturedImageEntity.fromCapturedImage(image)
            withContext(Dispatchers.IO) {
                dao.insert(entity)
            }
            _images.value = _images.value + image
            startTimer(image)
        }
    }

    fun removeImage(id: UUID) {
        scope.launch {
            timerJobs[id]?.cancel()
            timerJobs.remove(id)
            withContext(Dispatchers.IO) {
                dao.deleteById(id.toString())
            }
            _images.value = _images.value.filter { it.id != id }
            scope.launch {
                cacheMutex.withLock {
                    bitmapCache.remove(id)?.recycle()
                }
            }
        }
    }

    fun removeExpiredImages() {
        scope.launch {
            val now = Instant.now()
            val expired = _images.value.filter { !it.expiresAt.isAfter(now) }
            if (expired.isEmpty()) return@launch

            expired.forEach { image ->
                timerJobs[image.id]?.cancel()
                timerJobs.remove(image.id)
            }

            withContext(Dispatchers.IO) {
                dao.deleteExpired(now.toEpochMilli())
            }

            _images.value = _images.value.filter { it.expiresAt.isAfter(now) }

            cacheMutex.withLock {
                expired.forEach { image ->
                    bitmapCache.remove(image.id)?.recycle()
                }
            }
        }
    }

    fun clearAllImages() {
        scope.launch {
            timerJobs.values.forEach { it.cancel() }
            timerJobs.clear()

            withContext(Dispatchers.IO) {
                dao.deleteAll()
            }

            _images.value = emptyList()

            cacheMutex.withLock {
                bitmapCache.values.forEach { it.recycle() }
                bitmapCache.clear()
            }
        }
    }

    suspend fun getBitmap(image: CapturedImage): Bitmap? {
        cacheMutex.withLock {
            bitmapCache[image.id]?.let { return it }
        }
        return withContext(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeByteArray(
                image.imageData, 0, image.imageData.size
            )
            if (bitmap != null) {
                cacheMutex.withLock {
                    bitmapCache[image.id] = bitmap
                }
            }
            bitmap
        }
    }

    private fun startTimer(image: CapturedImage) {
        val remaining = image.expiresAt.toEpochMilli() - Instant.now().toEpochMilli()
        if (remaining <= 0) {
            removeImage(image.id)
            return
        }
        val job = scope.launch {
            delay(remaining)
            removeImage(image.id)
        }
        timerJobs[image.id] = job
    }

    override fun onTrimMemory(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE) {
            scope.launch {
                cacheMutex.withLock {
                    bitmapCache.values.forEach { it.recycle() }
                    bitmapCache.clear()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        scope.launch {
            cacheMutex.withLock {
                bitmapCache.values.forEach { it.recycle() }
                bitmapCache.clear()
            }
        }
    }

    fun release() {
        timerJobs.values.forEach { it.cancel() }
        timerJobs.clear()
        scope.launch {
            cacheMutex.withLock {
                bitmapCache.values.forEach { it.recycle() }
                bitmapCache.clear()
            }
        }
        scope.cancel()
    }

    companion object {
        private const val MAX_CACHE_SIZE = 2
    }
}
