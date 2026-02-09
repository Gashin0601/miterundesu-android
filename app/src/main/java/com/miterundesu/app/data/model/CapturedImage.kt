package com.miterundesu.app.data.model

import java.time.Instant
import java.util.UUID

data class CapturedImage(
    val id: UUID = UUID.randomUUID(),
    val capturedAt: Instant = Instant.now(),
    val expiresAt: Instant = Instant.now().plusSeconds(600),
    internal val imageData: ByteArray
) {
    val isExpired: Boolean
        get() = Instant.now() >= expiresAt

    val remainingTime: Double
        get() = (expiresAt.toEpochMilli() - Instant.now().toEpochMilli()) / 1000.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CapturedImage
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
