package com.miterundesu.app.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.miterundesu.app.data.model.CapturedImage
import java.time.Instant
import java.util.UUID

@Entity(tableName = "captured_images")
data class CapturedImageEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "image_data")
    val imageData: ByteArray,
    @ColumnInfo(name = "captured_at")
    val capturedAt: Long,
    @ColumnInfo(name = "expiration_date")
    val expirationDate: Long
) {
    fun toCapturedImage(): CapturedImage {
        return CapturedImage(
            id = UUID.fromString(id),
            capturedAt = Instant.ofEpochMilli(capturedAt),
            expiresAt = Instant.ofEpochMilli(expirationDate),
            imageData = imageData
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CapturedImageEntity
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        fun fromCapturedImage(image: CapturedImage): CapturedImageEntity {
            return CapturedImageEntity(
                id = image.id.toString(),
                imageData = image.imageData,
                capturedAt = image.capturedAt.toEpochMilli(),
                expirationDate = image.expiresAt.toEpochMilli()
            )
        }
    }
}
