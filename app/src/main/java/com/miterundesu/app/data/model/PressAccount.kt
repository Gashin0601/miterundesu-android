package com.miterundesu.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Serializable
data class PressAccount(
    val id: String = "",
    @SerialName("user_id")
    val userId: String = "",
    @SerialName("organization_name")
    val organizationName: String = "",
    @SerialName("organization_type")
    val organizationType: String? = null,
    @SerialName("contact_person")
    val contactPerson: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerialName("approved_by")
    val approvedBy: String? = null,
    @SerialName("approved_at")
    val approvedAt: String? = null,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("is_active")
    val isActive: Boolean = true,
    @SerialName("last_login_at")
    val lastLoginAt: String? = null,
    @SerialName("created_at")
    val createdAt: String = "",
    @SerialName("updated_at")
    val updatedAt: String = ""
) {
    val status: PressAccountStatus
        get() {
            if (!isActive) return PressAccountStatus.DEACTIVATED
            val expires = expiresAt ?: return PressAccountStatus.ACTIVE
            return try {
                val expirationInstant = ZonedDateTime.parse(expires).toInstant()
                if (expirationInstant.isBefore(Instant.now())) {
                    PressAccountStatus.EXPIRED
                } else {
                    PressAccountStatus.ACTIVE
                }
            } catch (_: Exception) {
                PressAccountStatus.ACTIVE
            }
        }

    val isValid: Boolean
        get() = status == PressAccountStatus.ACTIVE

    val daysUntilExpiration: Long?
        get() {
            val expires = expiresAt ?: return null
            return try {
                val expirationInstant = ZonedDateTime.parse(expires).toInstant()
                val now = Instant.now()
                if (expirationInstant.isAfter(now)) {
                    ChronoUnit.DAYS.between(now, expirationInstant)
                } else {
                    0L
                }
            } catch (_: Exception) {
                null
            }
        }

    val expirationDisplayString: String
        get() {
            val expires = expiresAt ?: return "無期限"
            return try {
                val zonedDateTime = ZonedDateTime.parse(expires)
                val localDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                localDate.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
            } catch (_: Exception) {
                expires
            }
        }

    val summary: String
        get() {
            val parts = mutableListOf<String>()
            parts.add(organizationName)
            if (!contactPerson.isNullOrBlank()) parts.add(contactPerson)
            parts.add(statusMessage)
            val expiresStr = expirationDisplayString
            if (expiresStr != "無期限") parts.add("→ $expiresStr")
            return parts.joinToString(" | ")
        }

    val statusMessage: String
        get() = when (status) {
            PressAccountStatus.ACTIVE -> {
                val days = daysUntilExpiration
                if (days != null) {
                    "アクティブ (残り${days}日)"
                } else {
                    "アクティブ"
                }
            }
            PressAccountStatus.EXPIRED -> "期限切れ"
            PressAccountStatus.DEACTIVATED -> "無効化済み"
        }
}

enum class PressAccountStatus {
    ACTIVE,
    EXPIRED,
    DEACTIVATED
}
