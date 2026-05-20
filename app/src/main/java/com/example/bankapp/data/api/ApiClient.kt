package com.example.bankapp.data.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.serialization.json.Json

/**
 * HTTP клиент для работы с API банка
 */
class ApiClient {
    
    private val client: HttpClient
    
    // Текущий access токен
    var accessToken: String? = null
        private set
    
    // Refresh токен
    var refreshToken: String? = null
        private set
    
    init {
        client = createHttpClient()
    }
    
    private fun createHttpClient(): HttpClient {
        return HttpClient(CIO) {
            engine {
                requestTimeout = ApiConfig.READ_TIMEOUT_SECONDS * 1000
            }
            
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.BODY
            }
            
            install(HttpTimeout) {
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT_SECONDS * 1000
                requestTimeoutMillis = ApiConfig.READ_TIMEOUT_SECONDS * 1000
                socketTimeoutMillis = ApiConfig.WRITE_TIMEOUT_SECONDS * 1000
            }
        }
    }
    
    /**
     * Обновить токены аутентификации
     */
    fun setTokens(accessToken: String, refreshToken: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }
    
    /**
     * Очистить токены (при выходе)
     */
    fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
    
    /**
     * Получить заголовки авторизации
     */
    private fun getAuthHeaders(): Map<String, String> {
        return accessToken?.let { 
            mapOf("Authorization" to "Bearer $it") 
        } ?: emptyMap()
    }
    
    /**
     * Получить заголовки с idempotency key
     */
    private fun getIdempotencyHeaders(idempotencyKey: String): Map<String, String> {
        return getAuthHeaders() + mapOf("X-Idempotency-Key" to idempotencyKey)
    }
    
    // ==================== HEALTH & ROOT ====================
    
    suspend fun healthCheck(): Result<String> {
        return try {
            val response = client.get("${ApiConfig.baseUrl}/health")
            if (response.status.isSuccess()) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Health check failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== AUTHENTICATION ====================
    
    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = client.post("${ApiConfig.baseUrl}${ApiConfig.BASE_PATH}/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            if (response.status.isSuccess()) {
                val result = response.body<RegisterResponse>()
                Result.success(result)
            } else {
                Result.failure(Exception("Registration failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(request: LoginRequest): Result<TokenResponse> {
        return try {
            val response = client.post("${ApiConfig.baseUrl}${ApiConfig.BASE_PATH}/auth/login") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "username" to request.username,
                        "password" to request.password,
                        "grant_type" to "password"
                    ).formUrlEncode()
                )
            }
            if (response.status.isSuccess()) {
                val tokens = response.body<TokenResponse>()
                setTokens(tokens.accessToken, tokens.refreshToken)
                Result.success(tokens)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun refreshToken(): Result<TokenResponse> {
        val refresh = refreshToken ?: return Result.failure(Exception("No refresh token available"))
        
        return try {
            val response = client.post("${ApiConfig.baseUrl}${ApiConfig.BASE_PATH}/auth/refresh") {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refresh))
            }
            if (response.status.isSuccess()) {
                val tokens = response.body<TokenResponse>()
                setTokens(tokens.accessToken, tokens.refreshToken)
                Result.success(tokens)
            } else {
                Result.failure(Exception("Token refresh failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ==================== FIDO2 / PASSKEYS ====================
    
    suspend fun getFidoRegisterChallenge(userId: String, username: String): Result<Map<String, Any>> {
        return makeRequest(
            method = HttpMethod.Post,
            path = "/auth/fido/register/challenge?user_id=$userId&username=$username",
            body = null,
            requiresAuth = false
        )
    }
    
    suspend fun verifyFidoRegister(userId: String, deviceId: String?, request: Map<String, Any>): Result<Map<String, Any>> {
        val deviceParam = deviceId?.let { "&device_id=$it" } ?: ""
        return makeRequest(
            method = HttpMethod.Post,
            path = "/auth/fido/register/verify?user_id=$userId$deviceParam",
            body = request,
            requiresAuth = false
        )
    }
    
    suspend fun getFidoLoginChallenge(userId: String): Result<Map<String, Any>> {
        return makeRequest(
            method = HttpMethod.Post,
            path = "/auth/fido/login/challenge?user_id=$userId",
            body = null,
            requiresAuth = false
        )
    }
    
    suspend fun verifyFidoLogin(userId: String, request: Map<String, Any>): Result<TokenResponse> {
        return makeRequest(
            method = HttpMethod.Post,
            path = "/auth/fido/login/verify?user_id=$userId",
            body = request,
            requiresAuth = false
        )
    }
    
    suspend fun getFidoCredentials(userId: String): Result<List<Map<String, Any>>> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/auth/fido/credentials?user_id=$userId"
        )
    }
    
    suspend fun deleteFidoCredential(userId: String, credentialId: String): Result<Unit> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Delete,
            path = "/auth/fido/credentials/$credentialId?user_id=$userId"
        )
    }
    
    // ==================== ACCOUNTS ====================
    
    suspend fun createAccount(userId: String, currency: String = "USD", initialBalance: String = "0.00"): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/accounts/",
            body = mapOf(
                "user_id" to userId,
                "currency" to currency,
                "initial_balance" to initialBalance
            )
        )
    }
    
    suspend fun getAccounts(page: Int = 0, pageSize: Int = 20): Result<AccountListResponse> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/accounts/?skip=$page&limit=$pageSize"
        )
    }
    
    suspend fun getAccount(accountId: String): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/accounts/$accountId"
        )
    }
    
    suspend fun getAccountByUserId(userId: String): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/accounts/user/$userId"
        )
    }
    
    suspend fun updateAccount(accountId: String, request: Map<String, Any>): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Patch,
            path = "/accounts/$accountId",
            body = request
        )
    }
    
    suspend fun deposit(
        accountId: String, 
        amount: String, 
        idempotencyKey: String,
        description: String? = null,
        reference: String? = null
    ): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/accounts/$accountId/deposit",
            body = mapOf(
                "amount" to amount,
                "description" to (description ?: ""),
                "reference" to (reference ?: "")
            ).filterValues { it.isNotEmpty() },
            additionalHeaders = getIdempotencyHeaders(idempotencyKey)
        )
    }
    
    suspend fun withdraw(
        accountId: String, 
        amount: String, 
        idempotencyKey: String,
        description: String? = null,
        reference: String? = null
    ): Result<AccountDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/accounts/$accountId/withdraw",
            body = mapOf(
                "amount" to amount,
                "description" to (description ?: ""),
                "reference" to (reference ?: "")
            ).filterValues { it.isNotEmpty() },
            additionalHeaders = getIdempotencyHeaders(idempotencyKey)
        )
    }
    
    suspend fun deactivateAccount(accountId: String): Result<Unit> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Delete,
            path = "/accounts/$accountId"
        )
    }
    
    // ==================== TRANSACTIONS ====================
    
    suspend fun transfer(
        toAccountId: String,
        amount: Double,
        idempotencyKey: String,
        description: String? = null,
        currentUserId: String
    ): Result<TransactionDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/transactions/transfer?current_user=$currentUserId",
            body = TransferRequest(toAccountId, amount, description = description),
            additionalHeaders = getIdempotencyHeaders(idempotencyKey)
        )
    }
    
    suspend fun depositTransaction(
        accountId: String,
        amount: String,
        idempotencyKey: String,
        currency: String = "USD",
        description: String? = null,
        reference: String? = null
    ): Result<TransactionDto> {
        val params = buildString {
            append("account_id=$accountId")
            append("&amount=$amount")
            append("&currency=$currency")
            description?.let { append("&description=${it.take(500)}") }
            reference?.let { append("&reference=${it.take(255)}") }
        }
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/transactions/deposit?$params",
            body = null,
            additionalHeaders = getIdempotencyHeaders(idempotencyKey)
        )
    }
    
    suspend fun getTransactions(page: Int = 0, pageSize: Int = 20): Result<TransactionListResponse> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/transactions/?skip=$page&limit=$pageSize"
        )
    }
    
    suspend fun getTransaction(transactionId: String): Result<TransactionDto> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/transactions/$transactionId"
        )
    }
    
    suspend fun getAccountTransactions(accountId: String, page: Int = 0, pageSize: Int = 20): Result<TransactionListResponse> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/transactions/account/$accountId?skip=$page&limit=$pageSize"
        )
    }
    
    // ==================== TELEMETRY & SECURITY ====================
    
    suspend fun collectSessionTelemetry(data: Map<String, Any>): Result<Unit> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/telemetry/session",
            body = data
        )
    }
    
    suspend fun getSecurityHistory(userId: String, limit: Int = 50): Result<List<Map<String, Any>>> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/telemetry/security/history?user_id=$userId&limit=$limit"
        )
    }
    
    suspend fun getUserDevices(userId: String): Result<List<Map<String, Any>>> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/telemetry/devices?user_id=$userId"
        )
    }
    
    suspend fun revokeDevice(userId: String, deviceId: String): Result<Unit> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Delete,
            path = "/telemetry/devices/$deviceId?user_id=$userId"
        )
    }
    
    suspend fun trustDevice(userId: String, deviceId: String): Result<Unit> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Post,
            path = "/telemetry/devices/$deviceId/trust?user_id=$userId"
        )
    }
    
    suspend fun getCertificatePinningConfig(): Result<CertificatePinningConfig> {
        return makeAuthenticatedRequest(
            method = HttpMethod.Get,
            path = "/telemetry/certificate-pinning"
        )
    }
    
    // ==================== HELPER METHODS ====================
    
    private suspend inline fun <reified T> makeRequest(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        requiresAuth: Boolean = true,
        additionalHeaders: Map<String, String> = emptyMap()
    ): Result<T> {
        return try {
            val response = client.request("${ApiConfig.baseUrl}${ApiConfig.BASE_PATH}$path") {
                this.method = method
                contentType(ContentType.Application.Json)
                
                // Добавляем заголовки авторизации если требуется
                if (requiresAuth) {
                    accessToken?.let { 
                        header("Authorization", "Bearer $it") 
                    }
                }
                
                // Добавляем дополнительные заголовки
                additionalHeaders.forEach { (key, value) ->
                    header(key, value)
                }
                
                // Устанавливаем тело запроса если есть
                body?.let { setBody(it) }
            }
            
            if (response.status.isSuccess()) {
                if (T::class == Unit::class) {
                    @Suppress("UNCHECKED_CAST")
                    Result.success(Unit as T)
                } else {
                    Result.success(response.body<T>())
                }
            } else {
                Result.failure(Exception("Request failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend inline fun <reified T> makeAuthenticatedRequest(
        method: HttpMethod,
        path: String,
        body: Any? = null,
        additionalHeaders: Map<String, String> = emptyMap()
    ): Result<T> {
        return makeRequest(method, path, body, requiresAuth = true, additionalHeaders)
    }
    
    /**
     * Закрыть клиент и освободить ресурсы
     */
    fun close() {
        client.close()
    }
}

// Глобальный экземпляр клиента
object ApiClientProvider {
    val client: ApiClient by lazy { ApiClient() }
}
