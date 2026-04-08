package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.screens.HomeScreen
import com.example.bankapp.ui.screens.auth.TwoFactorAuthScreen
import com.example.bankapp.ui.screens.family.FamilyScreen
import com.example.bankapp.ui.screens.help.HelpScreen
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
                    BankApp()
                }
            }
        }
    }
}

@Composable
fun BankApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val repository = remember { FamilyRepository() }

    if (!isAuthenticated) {
        TwoFactorAuthScreen(
            onAuthSuccess = { isAuthenticated = true },
            onAuthFailure = { /* Обработка ошибки */ }
        )
    } else {
        MainNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            repository = repository
        )
    }
}

@Composable
fun MainNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    repository: FamilyRepository
) {
    when (selectedTab) {
        0 -> HomeScreen()
        1 -> FamilyScreen(repository = repository)
        2 -> HelpScreen()
        3 -> ProfileScreen()
    }
}

@androidx.compose.runtime.Composable
fun ProfileScreen() {
    // Заглушка для экрана профиля
    androidx.compose.material3.Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { androidx.compose.material3.Text("Профиль") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Person,
                contentDescription = null,
                modifier = androidx.compose.ui.Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
            androidx.compose.material3.Text(
                text = "Александр Петров",
                style = MaterialTheme.typography.headlineSmall
            )
            androidx.compose.material3.Text(
                text = "+7 (999) 123-45-67",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
