package com.example.bankapp

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.screens.HomeScreen
import com.example.bankapp.ui.screens.auth.TwoFactorAuthScreen
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

@Composable
fun bankApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }
    
    val repository = remember { FamilyRepository() }

    if (!isAuthenticated) {
        TwoFactorAuthScreen(
            onAuthSuccess = { isAuthenticated = true },
            onAuthFailure = { /* Обработка ошибки */ }
        )
    } else {
        mainNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            repository = repository,
            onOpenSettings = { showSettings = true }
        )
        
        if (showSettings) {
            SettingsScreen(
                repository = repository,
                onNavigateBack = { showSettings = false }
            )
        }
    }
}

@Composable
fun mainNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    repository: FamilyRepository,
    onOpenSettings: () -> Unit
) {
    when (selectedTab) {
        0 -> HomeScreen(repository = repository, onOpenSettings = onOpenSettings)
        1 -> FamilyScreen(repository = repository)
        2 -> HelpScreen()
        3 -> profileScreen(repository = repository, onOpenSettings = onOpenSettings)
    }
}

@Composable
fun profileScreen(
    repository: FamilyRepository,
    onOpenSettings: () -> Unit
) {
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
                text = "Александр Петров",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "+7 (999) 123-45-67",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопка выхода
            OutlinedButton(
                onClick = {
                    repository.logout()
                    // Здесь должна быть логика возврата к экрану аутентификации
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Выйти")
            }
        }
    }
}
