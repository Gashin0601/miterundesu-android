package com.miterundesu.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Serializable
data class PressAccount(
    val id: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("organization_name")
    val organizationName: String,
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
    val isActive: Boolean,
    @SerialName("last_login_at")
    val lastLoginAt: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
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

    /// 有効期限までの残り日数
    val daysUntilExpiration: Int
        get() {
            val expires = expiresAt ?: return 0
            return try {
                val expirationInstant = ZonedDateTime.parse(expires).toInstant()
                val now = Instant.now()
                val days = ChronoUnit.DAYS.between(now, expirationInstant)
                maxOf(0, days.toInt())
            } catch (_: Exception) {
                0
            }
        }

    /// 有効期限の表示用文字列（日本語）
    val expirationDisplayString: String
        get() {
            val expires = expiresAt ?: return "無期限"
            return try {
                val zonedDateTime = ZonedDateTime.parse(expires)
                val localDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN)
                localDate.format(formatter)
            } catch (_: Exception) {
                expires
            }
        }

    /// 承認日時の表示用文字列（日本語）
    val approvalDisplayString: String?
        get() {
            val approved = approvedAt ?: return null
            return try {
                val zonedDateTime = ZonedDateTime.parse(approved)
                val localDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.JAPAN)
                localDate.format(formatter)
            } catch (_: Exception) {
                approved
            }
        }

    /// 最終ログイン日時の表示用文字列（日本語）
    val lastLoginDisplayString: String?
        get() {
            val lastLogin = lastLoginAt ?: return null
            return try {
                val zonedDateTime = ZonedDateTime.parse(lastLogin)
                val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日 H:mm", Locale.JAPAN)
                localDateTime.format(formatter)
            } catch (_: Exception) {
                lastLogin
            }
        }

    /// 状態に応じたメッセージ
    val statusMessage: String
        get() = when (status) {
            PressAccountStatus.ACTIVE -> {
                val daysLeft = daysUntilExpiration
                if (daysLeft <= 7) {
                    "プレスモードは有効です。（残り${daysLeft}日）"
                } else {
                    "プレスモードは有効です。"
                }
            }
            PressAccountStatus.EXPIRED ->
                "プレスモードの有効期限が切れています。\n必要な場合は再申請してください。\n利用期間: $expirationDisplayString"
            PressAccountStatus.DEACTIVATED ->
                "このデバイスのプレスモードは無効化されています。"
        }

    /// アカウント情報の概要
    val summary: String
        get() {
            var info = "組織: $organizationName"
            if (!contactPerson.isNullOrBlank()) {
                info += "\n担当者: $contactPerson"
            }
            info += "\n有効期限: $expirationDisplayString"
            val lastLogin = lastLoginDisplayString
            if (lastLogin != null) {
                info += "\n最終ログイン: $lastLogin"
            }
            return info
        }
}

enum class PressAccountStatus {
    ACTIVE,
    EXPIRED,
    DEACTIVATED
}

@Serializable
data class PressLoginResponse(
    val account: PressAccount,
    val success: Boolean,
    val message: String? = null
)
