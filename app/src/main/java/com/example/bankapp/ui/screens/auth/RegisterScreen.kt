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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.api.ApiClientProvider
import com.example.bankapp.data.api.RegisterRequest
import kotlinx.coroutines.launch

/**
 * Экран регистрации нового пользователя
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistrationSuccess: (String) -> Unit, // Возвращает userId для установки PIN
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val apiClient = remember { ApiClientProvider.client }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Регистрация") },
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
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Логотип и заголовок
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Создание аккаунта",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Заполните форму для регистрации",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Поле ввода Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                placeholder = { Text("example@mail.com") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && email.isEmpty()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода имени
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Имя") },
                placeholder = { Text("Иван") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода фамилии
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Фамилия") },
                placeholder = { Text("Иванов") },
                leadingIcon = {
                    Icon(Icons.Default.PersonOutline, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода телефона
            OutlinedTextField(
                value = phone,
                onValueChange = { 
                    if (it.length <= 15) {
                        phone = it.filter { char -> char.isDigit() || char == '+' }
                    }
                },
                label = { Text("Телефон") },
                placeholder = { Text("+79991234567") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода имени пользователя (username)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Имя пользователя") },
                placeholder = { Text("ivan_ivanov") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                supportingText = { 
                    Text("Будет использоваться для входа") 
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле ввода пароля
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Пароль") },
                placeholder = { Text("Минимум 8 символов") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showPassword) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (showPassword) PasswordVisualTransformation() else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && password.isNotEmpty() && password.length < 8
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Поле подтверждения пароля
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Подтвердите пароль") },
                placeholder = { Text("Повторите пароль") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showConfirmPassword) "Скрыть пароль" else "Показать пароль"
                        )
                    }
                },
                visualTransformation = if (showConfirmPassword) PasswordVisualTransformation() else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = errorMessage != null && confirmPassword.isNotEmpty() && password != confirmPassword
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
            
            // Кнопка регистрации
            Button(
                onClick = {
                    errorMessage = null
                    
                    // Валидация
                    when {
                        email.isBlank() -> {
                            errorMessage = "Введите email"
                            return@Button
                        }
                        firstName.isBlank() -> {
                            errorMessage = "Введите имя"
                            return@Button
                        }
                        lastName.isBlank() -> {
                            errorMessage = "Введите фамилию"
                            return@Button
                        }
                        username.isBlank() -> {
                            errorMessage = "Введите имя пользователя"
                            return@Button
                        }
                        username.length < 3 -> {
                            errorMessage = "Имя пользователя должно содержать минимум 3 символа"
                            return@Button
                        }
                        password.length < 8 -> {
                            errorMessage = "Пароль должен содержать минимум 8 символов"
                            return@Button
                        }
                        password.length > 128 -> {
                            errorMessage = "Пароль не должен превышать 128 символов"
                            return@Button
                        }
                        password != confirmPassword -> {
                            errorMessage = "Пароли не совпадают"
                            return@Button
                        }
                    }
                    
                    isLoading = true
                    scope.launch {
                        try {
                            val registerRequest = RegisterRequest(
                                email = email.trim(),
                                password = password,
                                firstName = firstName.trim().takeIf { it.isNotBlank() },
                                lastName = lastName.trim().takeIf { it.isNotBlank() },
                                phone = phone.trim().takeIf { it.isNotBlank() },
                                username = username.trim()
                            )
                            
                            val result = apiClient.register(registerRequest)
                            
                            result.fold(
                                onSuccess = { tokens ->
                                    // Регистрация успешна - переходим к установке PIN
                                    onRegistrationSuccess(username.trim())
                                },
                                onFailure = { error ->
                                    errorMessage = "Ошибка регистрации: ${error.message}"
                                    isLoading = false
                                }
                            )
                        } catch (e: Exception) {
                            errorMessage = "Ошибка: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && 
                        email.isNotBlank() && 
                        firstName.isNotBlank() && 
                        lastName.isNotBlank() && 
                        username.isNotBlank() && 
                        password.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Зарегистрироваться", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка "Уже есть аккаунт?"
            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Уже есть аккаунт? Войти")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        text = "🔒 Безопасная регистрация",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "После регистрации вы установите PIN-код\nдля быстрого входа в приложение",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
