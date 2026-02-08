package com.miterundesu.app.data.model

import java.time.Instant
import java.util.UUID

data class CapturedImage(
    val id: UUID = UUID.randomUUID(),
    val capturedAt: Instant = Instant.now(),
    val expiresAt: Instant = Instant.now().plusSeconds(600),
    val imageData: ByteArray
) {
    val isExpired: Boolean
        get() = expiresAt <= Instant.now()

    val remainingTime: Long
        get() = maxOf(0L, expiresAt.toEpochMilli() - Instant.now().toEpochMilli())

    val remainingMinutes: Int
        get() = (remainingTime / 60000).toInt()

    val remainingSeconds: Int
        get() = ((remainingTime % 60000) / 1000).toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CapturedImage
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
