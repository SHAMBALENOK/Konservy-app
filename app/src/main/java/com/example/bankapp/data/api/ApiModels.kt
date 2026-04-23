package com.example.bankapp.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация API сервера
 */
object ApiConfig {
    // Базовый URL сервера - может быть изменён в настройках приложения
    var baseUrl: String = "http://localhost:8080"
        private set
    
    const val API_VERSION = "v1"
    const val BASE_PATH = "/api/$API_VERSION"
    
    // Таймауты
    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 60L
    
    /**
     * Обновление базового URL (вызывается из настроек)
     */
    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl.trimEnd('/')
    }
    
    /**
     * Получить полный URL для endpoint
     */
    fun getEndpoint(path: String): String {
        return "$baseUrl$BASE_PATH$path"
    }
}

/**
 * Ответ токенов аутентификации
 */
@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("token_type")
    val tokenType: String = "Bearer",
    @SerialName("expires_in")
    val expiresIn: Long
)

/**
 * Запрос на регистрацию пользователя
 */
@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

/**
 * Запрос на логин
 */
@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * Запрос на обновление токена
 */
@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token")
    val refreshToken: String
)

/**
 * Данные пользователя
 */
@Serializable
data class UserData(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val createdAt: String
)

/**
 * Модель счёта
 */
@Serializable
data class AccountDto(
    val id: String,
    val userId: String,
    val accountNumber: String,
    val balance: Double,
    val currency: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String? = null
)

/**
 * Запрос на создание счёта
 */
@Serializable
data class CreateAccountRequest(
    val currency: String = "RUB",
    val initialDeposit: Double? = null
)

/**
 * Запрос на пополнение/снятие
 */
@Serializable
data class AmountRequest(
    val amount: Double,
    val description: String? = null
)

/**
 * Модель транзакции
 */
@Serializable
data class TransactionDto(
    val id: String,
    val type: String,
    val fromAccountId: String?,
    val toAccountId: String?,
    val amount: Double,
    val currency: String,
    val status: String,
    val description: String?,
    val createdAt: String
)

/**
 * Запрос на перевод
 */
@Serializable
data class TransferRequest(
    @SerialName("from_account_id")
    val fromAccountId: String,
    @SerialName("to_account_id")
    val toAccountId: String,
    val amount: Double,
    val description: String? = null
)

/**
 * Запрос FIDO2 регистрации - вызов
 */
@Serializable
data class FidoChallengeRequest(
    val username: String? = null
)

/**
 * Запрос FIDO2 регистрации - подтверждение
 */
@Serializable
data class FidoVerificationRequest(
    val credential: String,
    val clientDataJson: String,
    val attestationObject: String,
    val transports: List<String>? = null
)

/**
 * Запрос FIDO2 логина - подтверждение
 */
@Serializable
data class FidoAssertionRequest(
    val credentialId: String,
    val clientDataJson: String,
    val authenticatorData: String,
    val signature: String,
    val userHandle: String? = null
)

/**
 * Учётные данные FIDO2
 */
@Serializable
data class FidoCredential(
    val id: String,
    val name: String,
    val createdAt: String,
    val lastUsedAt: String?,
    val deviceType: String
)

/**
 * Страница пагинации
 */
@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int
)

/**
 * Ошибка API
 */
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

/**
 * Настройки безопасности устройства
 */
@Serializable
data class DeviceInfo(
    val id: String,
    val name: String,
    val type: String,
    val os: String,
    val lastSeenAt: String,
    val isTrusted: Boolean,
    val ipAddress: String? = null
)

/**
 * Событие безопасности
 */
@Serializable
data class SecurityEvent(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val deviceId: String?,
    val ipAddress: String?,
    val riskLevel: String
)

/**
 * Конфигурация certificate pinning
 */
@Serializable
data class CertificatePinningConfig(
    val enabled: Boolean,
    val pins: List<String>,
    val backupPins: List<String>,
    val maxAge: Long
)
