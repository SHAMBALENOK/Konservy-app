package com.example.bankapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.api.ApiClientProvider
import com.example.bankapp.data.api.ApiConfig
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.screens.HomeScreen
import com.example.bankapp.ui.screens.auth.LoginScreen
import com.example.bankapp.ui.screens.auth.PinEntryScreen
import com.example.bankapp.ui.screens.auth.RegisterScreen
import com.example.bankapp.ui.screens.auth.ServerSetupScreen
import com.example.bankapp.ui.screens.auth.SetPinScreen
import com.example.bankapp.ui.screens.family.FamilyScreen
import com.example.bankapp.ui.screens.help.HelpScreen
import com.example.bankapp.ui.screens.settings.SettingsScreen
import com.example.bankapp.ui.theme.BankAppTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализируем ApiConfig с контекстом приложения
        ApiConfig.init(applicationContext)
        
        setContent {
            BankAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    bankApp()
                }
            }
        }
    }
}

/**
 * Основное состояние приложения
 */
sealed class AppState {
    object Login : AppState()
    object Register : AppState()
    object SetPin : AppState()
    object ServerSetup : AppState()  // Экран настройки сервера (только при первой регистрации)
    object PinEntry : AppState()  // Экран ввода PIN для существующих пользователей
    object Authenticated : AppState()
}

@Composable
fun bankApp() {
    val context = LocalContext.current
    val prefs: SharedPreferences = context.getSharedPreferences("bank_app_prefs", Context.MODE_PRIVATE)
    val isPinSet = prefs.getBoolean("is_pin_set", false)
    val hasSeenSetup = prefs.getBoolean("has_seen_setup", false)
    val serverUrl = prefs.getString("server_url", null)
    
    // Определяем начальное состояние на основе наличия PIN и настройки сервера
    var appState by remember { 
        mutableStateOf<AppState>(
            if (!hasSeenSetup && serverUrl.isNullOrBlank()) {
                // Если настройка сервера не пройдена и URL не сохранён - показываем настройку сервера
                AppState.ServerSetup
            } else if (isPinSet) {
                // Если PIN установлен - показываем ввод PIN
                AppState.PinEntry
            } else {
                // Иначе - экран входа (не регистрации!)
                AppState.Login
            }
        )
    }
    var selectedTab by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }

    val repository = remember { FamilyRepository(context) }

    when (appState) {
        is AppState.ServerSetup -> {
            ServerSetupScreen(
                onServerConfigured = { 
                    // Сервер настроен - помечаем настройку как пройденную и переходим к регистрации/входу
                    prefs.edit().putBoolean("has_seen_setup", true).apply()
                    appState = AppState.Login  // Переходим к экрану входа/регистрации
                },
                onSkipSetup = {
                    // Пропустить настройку - используем URL по умолчанию и переходим к входу
                    prefs.edit().putBoolean("has_seen_setup", true).apply()
                    appState = AppState.Login  // Переходим к экрану входа/регистрации
                }
            )
        }
        is AppState.Login -> {
            LoginScreen(
                onLoginSuccess = { 
                    // После успешного входа по username/password - переходим в приложение
                    appState = AppState.Authenticated 
                },
                onNavigateToRegister = { appState = AppState.Register }
            )
        }
        is AppState.PinEntry -> {
            PinEntryScreen(
                onPinSuccess = {
                    // PIN верный - переходим в приложение
                    appState = AppState.Authenticated
                },
                onNavigateToLogin = {
                    // Пользователь хочет войти по username/password
                    appState = AppState.Login
                }
            )
        }
        is AppState.Register -> {
            RegisterScreen(
                onRegistrationSuccess = { registeredUsername ->
                    username = registeredUsername
                    appState = AppState.SetPin
                },
                onNavigateToLogin = { appState = AppState.Login }
            )
        }
        is AppState.SetPin -> {
            SetPinScreen(
                username = username,
                onPinSetSuccess = { 
                    // PIN установлен успешно - показываем настройку сервера (только первый раз)
                    val hasSeenSetup = prefs.getBoolean("has_seen_setup", false)
                    if (!hasSeenSetup) {
                        appState = AppState.ServerSetup
                    } else {
                        appState = AppState.Authenticated
                    }
                },
                onCancel = { appState = AppState.Login }
            )
        }
        is AppState.Authenticated -> {
            mainNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                repository = repository,
                onOpenSettings = { showSettings = true },
                onLogout = {
                    // Выход из приложения
                    repository.logout()
                    appState = AppState.Login
                }
            )
            
            if (showSettings) {
                SettingsScreen(
                    repository = repository,
                    onNavigateBack = { showSettings = false },
                    onChangeServerUrl = {
                        showSettings = false
                        appState = AppState.ServerSetup
                    }
                )
            }
        }
    }
}

@Composable
fun mainNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    repository: FamilyRepository,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var showDepositDialog by remember { mutableStateOf(false) }
    
    when (selectedTab) {
        0 -> HomeScreen(
            repository = repository, 
            onOpenSettings = onOpenSettings,
            onDeposit = { showDepositDialog = true }
        )
        1 -> FamilyScreen(repository = repository)
        2 -> HelpScreen()
        3 -> profileScreen(repository = repository, onOpenSettings = onOpenSettings, onLogout = onLogout)
    }
    
    // Диалог пополнения счёта
    if (showDepositDialog) {
        DepositDialog(
            repository = repository,
            onDismiss = { showDepositDialog = false },
            onDepositSuccess = { 
                showDepositDialog = false
                // Обновляем данные счетов после успешного пополнения
                // (теперь это делается внутри repository.deposit())
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun profileScreen(
    repository: FamilyRepository,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val username by repository.currentUsername.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = username.ifBlank { "Пользователь" },
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Авторизован в системе",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопка выхода
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositDialog(
    repository: FamilyRepository,
    onDismiss: () -> Unit,
    onDepositSuccess: () -> Unit
) {
    var amount by remember { mutableStateOf("10000") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccess by remember { mutableStateOf(false) }
    
    val accounts by repository.accounts.collectAsState()
    val selectedAccount = accounts.firstOrNull()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Пополнение счёта") },
        text = {
            Column {
                if (selectedAccount != null) {
                    Text(
                        text = "Счёт: ${selectedAccount.accountName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Сумма (RUB)") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage != null
                )
                
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (showSuccess) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Успешно пополнено на $amount RUB!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedAccount == null) {
                        errorMessage = "Счёт не найден"
                        return@Button
                    }
                    
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        errorMessage = "Введите корректную сумму"
                        return@Button
                    }
                    
                    isLoading = true
                    errorMessage = null
                    
                    // Запускаем пополнение в фоновом режиме
                    GlobalScope.launch {
                        try {
                            val result = repository.deposit(
                                accountId = selectedAccount.accountId,
                                amount = amountValue,
                                description = "Тестовое пополнение на $amountValue RUB"
                            )
                            
                            if (result.isSuccess) {
                                showSuccess = true
                                delay(1500)
                                onDepositSuccess()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Ошибка пополнения"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Неизвестная ошибка"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading && !showSuccess
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Пополнить")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}
