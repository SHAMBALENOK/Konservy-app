package com.example.bankapp.data.api

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Конфигурация API сервера
 */
object ApiConfig {
    // Базовый URL сервера - может быть изменён в настройках приложения
    var baseUrl: String = "http://localhost:8080"
        private set
    
    private var prefs: SharedPreferences? = null
    
    /**
     * Инициализация с контекстом для доступа к SharedPreferences
     */
    fun init(context: Context) {
        prefs = context.getSharedPreferences("bank_app_prefs", Context.MODE_PRIVATE)
        // Загружаем сохранённый URL при инициализации
        prefs?.getString("server_url", null)?.let { savedUrl ->
            if (savedUrl.isNotBlank()) {
                baseUrl = savedUrl
            }
        }
    }
    
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
        // Сохраняем URL в SharedPreferences
        prefs?.edit()?.putString("server_url", baseUrl)?.apply()
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
 * Ответ на регистрацию пользователя
 */
@Serializable
data class RegisterResponse(
    val message: String,
    val username: String
)

/**
 * Запрос на регистрацию пользователя
 */
@Serializable
data class RegisterRequest(
    val username: String,
    val password: String
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
 * Модель счёта - соответствует серверной AccountResponse
 */
@Serializable
data class AccountDto(
    val id: Int,
    @SerialName("account_id")
    val accountId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("account_number")
    val accountNumber: String,
    val balance: String,
    @SerialName("is_active")
    val isActive: Boolean = true,
    val currency: String = "USD",
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * Ответ списка счетов с пагинацией
 */
@Serializable
data class AccountListResponse(
    val items: List<AccountDto>,
    val total: Int,
    val page: Int,
    @SerialName("page_size")
    val pageSize: Int
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
 * Модель транзакции - соответствует серверной TransactionResponse
 */
@Serializable
data class TransactionDto(
    val id: Int,
    @SerialName("transaction_id")
    val transactionId: String,
    val type: String,
    val status: String,
    @SerialName("source_account_id")
    val sourceAccountId: String?,
    @SerialName("destination_account_id")
    val destinationAccountId: String?,
    val amount: String,
    val currency: String = "USD",
    val description: String? = null,
    val reference: String? = null,
    @SerialName("idempotency_key")
    val idempotencyKey: String? = null,
    @SerialName("failure_reason")
    val failureReason: String? = null,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("processed_at")
    val processedAt: String? = null
)

/**
 * Запрос на перевод - соответствует серверному TransferRequest
 * Сервер определяет source account из authenticated user
 */
@Serializable
data class TransferRequest(
    @SerialName("destination_account_id")
    val destinationAccountId: String,
    val amount: Double,
    val currency: String? = null,
    val description: String? = null,
    val reference: String? = null
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
 * Ответ списка транзакций с пагинацией
 */
@Serializable
data class TransactionListResponse(
    val items: List<TransactionDto>,
    val total: Int,
    val page: Int,
    @SerialName("page_size")
    val pageSize: Int
)

/**
 * Страница пагинации (устаревшая модель)
 */
@Serializable
data class PageResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    @SerialName("pageSize")
    val pageSize: Int,
    @SerialName("totalPages")
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
