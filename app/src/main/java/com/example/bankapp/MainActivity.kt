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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    
    // Определяем начальное состояние на основе наличия PIN
    var appState by remember { mutableStateOf<AppState>(if (isPinSet) AppState.PinEntry else AppState.Login) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    
    val repository = remember { FamilyRepository() }

    when (appState) {
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
        is AppState.ServerSetup -> {
            ServerSetupScreen(
                onServerConfigured = { 
                    // Сервер настроен - помечаем настройку как пройденную и переходим в приложение
                    prefs.edit().putBoolean("has_seen_setup", true).apply()
                    appState = AppState.Authenticated
                },
                onSkipSetup = {
                    // Пропустить настройку - используем URL по умолчанию
                    prefs.edit().putBoolean("has_seen_setup", true).apply()
                    appState = AppState.Authenticated
                }
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
    when (selectedTab) {
        0 -> HomeScreen(repository = repository, onOpenSettings = onOpenSettings)
        1 -> FamilyScreen(repository = repository)
        2 -> HelpScreen()
        3 -> profileScreen(repository = repository, onOpenSettings = onOpenSettings, onLogout = onLogout)
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
