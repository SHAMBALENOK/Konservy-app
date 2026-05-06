package com.example.bankapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.api.ApiConfig
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.screens.auth.ServerSetupScreen
import kotlinx.coroutines.launch

// Дополнительные импорты для Card
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults

/**
 * Экран настроек приложения
 * Позволяет указать URL сервера для API запросов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: FamilyRepository,
    onNavigateBack: () -> Unit,
    onChangeServerUrl: () -> Unit
) {
    val scope = rememberCoroutineScope()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Секция: Информация о сервере
            SettingsSection(title = "Сервер", icon = Icons.Default.Dns) {
                SettingItem(
                    title = "Текущий сервер",
                    subtitle = repository.getServerUrl(),
                    icon = Icons.Default.Dns,
                    trailingContent = {
                        Badge(
                            containerColor = if (repository.getServerUrl().contains("localhost") || 
                                               repository.getServerUrl().contains("127.0.0.1")) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = if (repository.getServerUrl().contains("localhost") || 
                                         repository.getServerUrl().contains("127.0.0.1")) "Local" else "Remote",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onChangeServerUrl,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сменить сервер")
                }
            }
            
            // Секция: Информация о приложении
            SettingsSection(title = "О приложении", icon = Icons.Default.Info) {
                SettingItem(
                    title = "Версия приложения",
                    subtitle = "1.0.0",
                    icon = Icons.Default.Android
                )
                
                SettingItem(
                    title = "API версия",
                    subtitle = ApiConfig.API_VERSION,
                    icon = Icons.Default.Code
                )
            }
            
            // Секция: Безопасность
            SettingsSection(title = "Безопасность", icon = Icons.Default.Security) {
                SettingItem(
                    title = "Двухфакторная аутентификация",
                    subtitle = "Включена",
                    icon = Icons.Default.Lock
                )
                
                SettingItem(
                    title = "Устройства",
                    subtitle = "Управление доверенными устройствами",
                    icon = Icons.Default.Devices,
                    onClick = { /* TODO: Navigate to devices screen */ }
                )
                
                SettingItem(
                    title = "История безопасности",
                    subtitle = "Просмотр событий безопасности",
                    icon = Icons.Default.History,
                    onClick = { /* TODO: Navigate to security history */ }
                )
            }
            
            // Секция: Действия
            SettingsSection(title = "Действия", icon = Icons.Default.Build) {
                Button(
                    onClick = {
                        // Проверка подключения к серверу
                        scope.launch {
                            // TODO: Implement health check
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Проверить подключение")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onChangeServerUrl,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сменить сервер")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = {
                        // Сброс настроек
                        repository.updateServerUrl("http://localhost:8080")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сбросить настройки")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Тестовая функция: накрутить деньги на счёт
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "💰 Тест: Начислить средства",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Добавить 10 000 ₽ на первый счёт для тестирования",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val accounts = repository.accounts.value
                                    if (accounts.isNotEmpty()) {
                                        val firstAccount = accounts.first()
                                        // Используем API для пополнения счёта
                                        repository.deposit(
                                            accountId = firstAccount.id.toString(),
                                            amount = 10000.0,
                                            description = "Тестовое начисление из настроек"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.align(androidx.compose.ui.Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+10 000 ₽")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (onClick != null) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) 
                else androidx.compose.ui.graphics.Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (trailingContent != null) {
                trailingContent()
            } else if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
