package com.example.bankapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.api.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.launch

/**
 * Экран настройки сервера для подключения к API
 * Отображается перед регистрацией или входом
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSetupScreen(
    onServerConfigured: () -> Unit,
    onSkipSetup: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(ApiConfig.baseUrl) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showServerUrl by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    // Проверка доступности сервера
    suspend fun checkServerAvailability(url: String): Pair<Boolean, String?> {
        val client = HttpClient(CIO) {
            install(HttpTimeout) {
                connectTimeoutMillis = 5000
                requestTimeoutMillis = 10000
            }
            followRedirects = true
        }
        
        return try {
            val cleanUrl = url.trimEnd('/')
            val healthUrl = "$cleanUrl/health"
            println("Checking server at: $healthUrl")
            
            val response = client.get(healthUrl)
            val statusCode = response.status.value
            println("Response status: $statusCode")
            
            val body = response.body<String>()
            println("Response body: $body")
            
            client.close()
            
            if (statusCode >= 200 && statusCode < 300) {
                Pair(true, null)
            } else {
                Pair(false, "Сервер вернул статус: $statusCode")
            }
        } catch (e: Exception) {
            println("Error checking server: ${e.message}")
            e.printStackTrace()
            Pair(false, "Ошибка подключения: ${e.message ?: "Неизвестная ошибка"}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройка сервера") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка и заголовок
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Подключение к серверу",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Укажите адрес сервера для подключения к банковскому API",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Поле ввода URL сервера
            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("URL сервера") },
                placeholder = { Text("http://localhost:8080") },
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null)
                },
                trailingIcon = {
                    if (serverUrl.isNotEmpty()) {
                        IconButton(onClick = { serverUrl = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Очистить")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null,
                supportingText = {
                    Text("Пример: http://localhost:8080 или https://api.yourbank.com")
                }
            )
            
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Быстрые пресеты
            Text(
                text = "Быстрый выбор:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Localhost
                FilterChip(
                    selected = serverUrl == "http://localhost:8080",
                    onClick = { serverUrl = "http://localhost:8080" },
                    label = { Text("Localhost") },
                    leadingIcon = if (serverUrl == "http://localhost:8080") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                
                // Development
                FilterChip(
                    selected = serverUrl == "http://10.0.2.2:8080",
                    onClick = { serverUrl = "http://10.0.2.2:8080" },
                    label = { Text("Dev") },
                    leadingIcon = if (serverUrl == "http://10.0.2.2:8080") {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                
                // Production
                FilterChip(
                    selected = serverUrl.startsWith("https://"),
                    onClick = { 
                        if (!serverUrl.startsWith("https://")) {
                            serverUrl = serverUrl.replace("http://", "https://")
                        }
                    },
                    label = { Text("HTTPS") },
                    leadingIcon = if (serverUrl.startsWith("https://")) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Информация о подключении
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Важная информация",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Для локальной разработки используйте http://localhost:8080\n• Для эмулятора Android используйте http://10.0.2.2:8080\n• Для продакшена используйте HTTPS",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Кнопка проверки и сохранения
            Button(
                onClick = {
                    errorMessage = null
                    
                    // Валидация URL
                    if (serverUrl.isBlank()) {
                        errorMessage = "Введите URL сервера"
                        return@Button
                    }
                    
                    val urlPattern = "^https?://[\\w.-]+(:\\d+)?\$".toRegex()
                    if (!urlPattern.matches(serverUrl.trimEnd('/'))) {
                        errorMessage = "Неверный формат URL. Пример: http://localhost:8080"
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        val normalizedUrl = serverUrl.trimEnd('/')
                        val (isAvailable, error) = checkServerAvailability(normalizedUrl)
                        
                        if (isAvailable) {
                            ApiConfig.updateBaseUrl(normalizedUrl)
                            isLoading = false
                            onServerConfigured()
                        } else {
                            errorMessage = error ?: "Сервер недоступен. Проверьте URL и подключение к сети."
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && serverUrl.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Проверка...", style = MaterialTheme.typography.titleMedium)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Проверить и сохранить", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Кнопка пропуска
            TextButton(
                onClick = onSkipSetup,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.SkipNext, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Пропустить и использовать по умолчанию")
            }
        }
    }
}
