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
import kotlinx.coroutines.launch
import android.content.Context
import android.content.SharedPreferences

/**
 * Экран установки PIN-кода для входа в приложение
 * Используется только один раз после регистрации
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinScreen(
    username: String,
    onPinSetSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var step by remember { mutableStateOf(PinSetupStep.ENTER_PIN) }
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Установка PIN-кода") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Закрыть")
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
                imageVector = Icons.Default.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (step) {
                    PinSetupStep.ENTER_PIN -> "Придумайте PIN-код"
                    PinSetupStep.CONFIRM_PIN -> "Подтвердите PIN-код"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Запомните этот код - он будет использоваться для быстрого входа в приложение",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Поле ввода PIN
            OutlinedTextField(
                value = if (step == PinSetupStep.ENTER_PIN) pin else confirmPin,
                onValueChange = { newValue ->
                    if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                        if (step == PinSetupStep.ENTER_PIN) {
                            pin = newValue
                        } else {
                            confirmPin = newValue
                        }
                    }
                },
                label = { Text("PIN-код") },
                placeholder = { Text("••••") },
                leadingIcon = {
                    Icon(Icons.Default.Security, contentDescription = null)
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

            // Индикатор прогресса
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Surface(
                        color = if (step == PinSetupStep.ENTER_PIN) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    Surface(
                        color = if (step == PinSetupStep.CONFIRM_PIN) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Spacer(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Кнопка продолжения
            Button(
                onClick = {
                    errorMessage = null
                    
                    if (step == PinSetupStep.ENTER_PIN) {
                        if (pin.length != 4) {
                            errorMessage = "PIN-код должен содержать 4 цифры"
                            return@Button
                        }
                        step = PinSetupStep.CONFIRM_PIN
                    } else {
                        if (confirmPin.length != 4) {
                            errorMessage = "PIN-код должен содержать 4 цифры"
                            return@Button
                        }
                        if (pin != confirmPin) {
                            errorMessage = "PIN-коды не совпадают"
                            confirmPin = ""
                            return@Button
                        }
                        
                        // Сохраняем PIN и завершаем установку
                        isLoading = true
                        scope.launch {
                            // Сохраняем PIN и username в SharedPreferences
                            val prefs: SharedPreferences = context.getSharedPreferences("bank_app_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putString("pin_code", pin)
                                .putString("username", username)
                                .putBoolean("is_pin_set", true)
                                .apply()
                            
                            isLoading = false
                            onPinSetSuccess()
                        }
                    }
                },
                enabled = (step == PinSetupStep.ENTER_PIN && pin.length == 4) ||
                         (step == PinSetupStep.CONFIRM_PIN && confirmPin.length == 4) && !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when (step) {
                            PinSetupStep.ENTER_PIN -> "Продолжить"
                            PinSetupStep.CONFIRM_PIN -> "Подтвердить"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка отмены
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отменить")
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
                        text = "🔐 Безопасность",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PIN-код хранится только на этом устройстве\nи используется для быстрого входа",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

enum class PinSetupStep {
    ENTER_PIN,
    CONFIRM_PIN
}
