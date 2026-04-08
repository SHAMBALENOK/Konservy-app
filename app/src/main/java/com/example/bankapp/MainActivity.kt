package com.example.bankapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
fun bankApp() {
    var isAuthenticated by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
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
            repository = repository
        )
    }
}

@Composable
fun mainNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    repository: FamilyRepository
) {
    when (selectedTab) {
        0 -> HomeScreen()
        1 -> FamilyScreen(repository = repository)
        2 -> HelpScreen()
        3 -> profileScreen()
    }
}

@Composable
fun profileScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
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
        }
    }
}
