package com.example.bankapp.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.api.ApiConfig
import com.example.bankapp.data.repository.FamilyRepository

/**
 * Экран настроек приложения
 * Позволяет указать URL сервера для API запросов
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: FamilyRepository,
    onNavigateBack: () -> Unit
) {
    var serverUrl by remember { mutableStateOf(repository.getServerUrl()) }
    var isEditingUrl by remember { mutableStateOf(false) }
    var showSaveConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
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
            // Секция: Сервер
            SettingsSection(title = "Сервер", icon = Icons.Default.Dns) {
                ServerUrlSetting(
                    serverUrl = serverUrl,
                    isEditing = isEditingUrl,
                    onEditToggle = { isEditingUrl = !isEditingUrl },
                    onUrlChange = { serverUrl = it },
                    onSave = {
                        if (serverUrl.isBlank()) {
                            errorMessage = "URL сервера не может быть пустым"
                            return@ServerUrlSetting
                        }
                        
                        // Валидация URL
                        if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                            errorMessage = "URL должен начинаться с http:// или https://"
                            return@ServerUrlSetting
                        }
                        
                        repository.updateServerUrl(serverUrl)
                        isEditingUrl = false
                        showSaveConfirmation = true
                        errorMessage = null
                    },
                    errorMessage = errorMessage
                )
                
                if (showSaveConfirmation) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Сервер обновлён: $serverUrl",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            TextButton(onClick = { showSaveConfirmation = false }) {
                                Text("OK")
                            }
                        }
                    }
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
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
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
                
                OutlinedButton(
                    onClick = {
                        // Сброс настроек
                        serverUrl = "http://localhost:8080"
                        repository.updateServerUrl(serverUrl)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сбросить настройки")
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

@Composable
fun ServerUrlSetting(
    serverUrl: String,
    isEditing: Boolean,
    onEditToggle: () -> Unit,
    onUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    errorMessage: String? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "URL сервера API",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isEditing) {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = onUrlChange,
                        placeholder = { Text("http://localhost:8080") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = errorMessage != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                } else {
                    Text(
                        text = serverUrl,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            if (isEditing) {
                IconButton(onClick = onSave) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Сохранить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onEditToggle) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Отмена",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                IconButton(onClick = onEditToggle) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Изменить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        if (!isEditing) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Измените URL сервера для подключения к другому экземпляру API",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
