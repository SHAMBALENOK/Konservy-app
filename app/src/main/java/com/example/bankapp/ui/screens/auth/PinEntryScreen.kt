package com.example.bankapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.SharedPreferences
import com.example.bankapp.data.repository.FamilyRepository
import kotlinx.coroutines.launch

/**
 * Экран ввода PIN-кода для входа в приложение
 * Показывается пользователям, у которых уже установлен PIN
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinEntryScreen(
    onPinSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val repository = remember { FamilyRepository() }
    
    // Получаем сохранённый PIN
    val prefs: SharedPreferences = context.getSharedPreferences("bank_app_prefs", Context.MODE_PRIVATE)
    val savedPin = prefs.getString("pin_code", "") ?: ""
    val savedUsername = prefs.getString("username", "") ?: ""
    
    // Восстанавливаем имя пользователя в репозитории при входе через PIN
    LaunchedEffect(savedUsername) {
        if (savedUsername.isNotEmpty()) {
            repository.setUsername(savedUsername)
        } else {
            // Если имя не сохранено, пытаемся получить его из токена
            scope.launch {
                val username = repository.fetchCurrentUsername()
                if (username.isNotEmpty()) {
                    prefs.edit().putString("username", username).apply()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Вход по PIN") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка и заголовок
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Введите PIN-код",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (savedUsername.isNotEmpty()) {
                Text(
                    text = "Пользователь: $savedUsername",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Для быстрого входа в приложение",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Поле ввода PIN
            OutlinedTextField(
                value = pin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        pin = newValue
                    }
                },
                label = { Text("PIN-код") },
                placeholder = { Text("••••") },
                leadingIcon = {
                    Icon(Icons.Default.Pin, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPin = !showPin }) {
                        Icon(
                            if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPin) "Скрыть PIN" else "Показать PIN"
                        )
                    }
                },
                visualTransformation = if (showPin) PasswordVisualTransformation() else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                isError = errorMessage != null
            )
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Кнопка входа
            Button(
                onClick = {
                    errorMessage = null
                    
                    if (pin.length != 4) {
                        errorMessage = "Введите 4-значный PIN-код"
                        return@Button
                    }
                    
                    isLoading = true
                    scope.launch {
                        // Проверяем PIN
                        if (pin == savedPin) {
                            // PIN верный - входим в приложение
                            onPinSuccess()
                        } else {
                            errorMessage = "Неверный PIN-код"
                            pin = ""
                        }
                        isLoading = false
                    }
                },
                enabled = pin.length == 4 && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Войти", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка "Войти по паролю"
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Войти по имени пользователя и паролю")
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Информация о безопасности
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 Быстрый вход",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Используйте PIN-код для быстрого доступа\nк вашему счёту",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
