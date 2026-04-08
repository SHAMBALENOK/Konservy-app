package com.example.bankapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Экран двухфакторной аутентификации
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorAuthScreen(
    onAuthSuccess: () -> Unit,
    onAuthFailure: (String) -> Unit
) {
    var step by remember { mutableStateOf(AuthStep.PASSWORD) }
    var password by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var smsSent by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(step) {
        if (step == AuthStep.SMS && !smsSent) {
            // Имитация отправки SMS
            isLoading = true
            delay(1000)
            smsSent = true
            countdown = 60
            isLoading = false
            
            // Запуск обратного отсчёта
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Безопасный вход") },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип и заголовок
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Двухфакторная\nаутентификация",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (step) {
                    AuthStep.PASSWORD -> "Введите ваш пароль для входа"
                    AuthStep.SMS -> "Введите код из SMS, отправленный на +7 (999) ***-**-67"
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            // Поле ввода пароля
            if (step == AuthStep.PASSWORD) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    placeholder = { Text("Введите пароль") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    trailingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (password.length < 4) {
                            errorMessage = "Пароль должен содержать минимум 4 символа"
                        } else {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                delay(1000) // Имитация проверки
                                isLoading = false
                                step = AuthStep.SMS
                            }
                        }
                    },
                    enabled = password.isNotEmpty() && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Продолжить", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Поле ввода SMS кода
            if (step == AuthStep.SMS) {
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { 
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            smsCode = it
                        }
                    },
                    label = { Text("Код из SMS") },
                    placeholder = { Text("------") },
                    leadingIcon = {
                        Icon(Icons.Default.Sms, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null,
                    enabled = countdown == 0 || smsCode.length < 6
                )
                
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Таймер повторной отправки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (countdown > 0) {
                        Text(
                            text = "Отправить повторно через ${countdown}с",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TextButton(
                            onClick = {
                                countdown = 60
                                smsCode = ""
                                errorMessage = null
                            }
                        ) {
                            Text("Отправить повторно")
                        }
                    }
                    
                    TextButton(
                        onClick = {
                            step = AuthStep.PASSWORD
                            password = ""
                            smsCode = ""
                            smsSent = false
                        }
                    ) {
                        Text("Изменить номер")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (smsCode.length != 6) {
                            errorMessage = "Код должен содержать 6 цифр"
                        } else {
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                delay(1000) // Имитация проверки
                                isLoading = false
                                
                                if (smsCode == "123456") {
                                    onAuthSuccess()
                                } else {
                                    errorMessage = "Неверный код подтверждения"
                                    smsCode = ""
                                }
                            }
                        }
                    },
                    enabled = smsCode.length == 6 && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Подтвердить", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            // Информация о безопасности
            Spacer(modifier = Modifier.height(32.dp))
            
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🔒 Ваши данные защищены",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Используется двухфакторная аутентификация\nи сквозное шифрование данных",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

enum class AuthStep {
    PASSWORD,
    SMS
}
