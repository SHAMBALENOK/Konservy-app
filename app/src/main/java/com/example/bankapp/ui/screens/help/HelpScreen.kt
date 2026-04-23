package com.example.bankapp.ui.screens.help

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView
import android.net.Uri
import android.widget.MediaController

data class HelpArticle(
    val id: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val content: String,
    val category: HelpCategory
)

enum class HelpCategory {
    GETTING_STARTED,
    SECURITY,
    FAMILY,
    TROUBLESHOOTING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen() {
    var selectedArticle by remember { mutableStateOf<HelpArticle?>(null) }
    var showVideoTutorial by remember { mutableStateOf(false) }

    val articles = listOf(
        HelpArticle(
            id = 1,
            icon = Icons.Default.PlayArrow,
            title = "Видео-инструкция",
            description = "Наглядное руководство по использованию приложения",
            content = "Посмотрите подробную видео-инструкцию о том, как пользоваться всеми функциями приложения.",
            category = HelpCategory.GETTING_STARTED
        ),
        HelpArticle(
            id = 2,
            icon = Icons.Default.Shield,
            title = "Как работает защита",
            description = "О системе безопасности и двухфакторной аутентификации",
            content = """
                Наше приложение использует современные методы защиты данных:
                
                • Двухфакторная аутентификация (пароль + SMS)
                • Сквозное шифрование всех данных
                • Биометрическая защита (Face ID / Touch ID)
                • Автоматическое блокирование подозрительных операций
                
                Все данные хранятся в зашифрованном виде и передаются по защищённым каналам связи.
            """.trimIndent(),
            category = HelpCategory.SECURITY
        ),
        HelpArticle(
            id = 3,
            icon = Icons.Default.FamilyRestroom,
            title = "Управление семьёй",
            description = "Добавление членов семьи и настройка контроля",
            content = """
                Для добавления члена семьи:
                
                1. Перейдите на вкладку "Семья"
                2. Нажмите кнопку "+" в правом верхнем углу
                3. Заполните информацию о члене семьи
                4. Выберите уровень доверия
                5. Настройте лимиты и уведомления
                
                Уровни доверия:
                • Полный — без ограничений
                • Стандарт — обычные лимиты
                • Ограниченный — строгий контроль
            """.trimIndent(),
            category = HelpCategory.FAMILY
        ),
        HelpArticle(
            id = 4,
            icon = Icons.Default.People,
            title = "Доверенные контакты",
            description = "Настройка уведомлений для близких",
            content = """
                Доверенные контакты получают уведомления о:
                
                • Подозрительных операциях
                • Крупных переводах
                • Длительных звонках с незнакомцами
                • Активности в мессенджерах
                
                Вы можете добавить несколько доверенных лиц. 
                При критических операциях уведомления отправляются всем сразу.
            """.trimIndent(),
            category = HelpCategory.SECURITY
        ),
        HelpArticle(
            id = 5,
            icon = Icons.Default.Warning,
            title = "Подозрительные операции",
            description = "Что делать при получении предупреждения",
            content = """
                Если вы получили уведомление о подозрительной операции:
                
                1. Немедленно свяжитесь с членом семьи
                2. Проверьте детали операции
                3. Примите решение:
                   - Подтвердить (если операция легитимна)
                   - Заблокировать (если есть сомнения)
                   
                При критическом уровне риска операция блокируется автоматически.
            """.trimIndent(),
            category = HelpCategory.TROUBLESHOOTING
        ),
        HelpArticle(
            id = 6,
            icon = Icons.Default.Settings,
            title = "Настройки безопасности",
            description = "Лимиты и параметры уведомлений",
            content = """
                В настройках безопасности вы можете настроить:
                
                • Максимальную сумму перевода
                • Максимальное снятие за день
                • Максимальную длительность звонка
                • Время активности в мессенджере
                • Получателей уведомлений
                
                Рекомендуется устанавливать лимиты исходя из потребностей вашей семьи.
            """.trimIndent(),
            category = HelpCategory.SECURITY
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Помощь и инструкции") },
                actions = {
                    IconButton(onClick = { showVideoTutorial = true }) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = "Видео")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Приветственный блок
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Чем мы можем помочь?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Выберите тему или посмотрите видео-инструкцию",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showVideoTutorial = true }
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Смотреть инструкцию")
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Статьи и руководства",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            items(articles) { article ->
                HelpArticleCard(
                    article = article,
                    onClick = { selectedArticle = article }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Контакты поддержки
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Support,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Нужна помощь?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Свяжитесь с нашей службой поддержки:\n• Телефон: 8 (800) XXX-XX-XX\n• Email: support@bankapp.ru\n• Чат: доступен 24/7",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Диалог с деталями статьи
    if (selectedArticle != null) {
        AlertDialog(
            onDismissRequest = { selectedArticle = null },
            icon = {
                Icon(
                    imageVector = selectedArticle!!.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(selectedArticle!!.title)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = selectedArticle!!.description,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Divider()
                    Text(
                        text = selectedArticle!!.content,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                    )
                }
            },
            confirmButton = {
                Button(onClick = { selectedArticle = null }) {
                    Text("Понятно")
                }
            }
        )
    }

    // Диалог видео-туториала
    if (showVideoTutorial) {
        VideoTutorialDialog(
            onDismiss = { showVideoTutorial = false }
        )
    }
}

@Composable
fun HelpArticleCard(
    article: HelpArticle,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = when (article.category) {
                    HelpCategory.GETTING_STARTED -> Color(0xFF2196F3)
                    HelpCategory.SECURITY -> Color(0xFF4CAF50)
                    HelpCategory.FAMILY -> Color(0xFFFF9800)
                    HelpCategory.TROUBLESHOOTING -> Color(0xFFF44336)
                }.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = article.icon,
                        contentDescription = null,
                        tint = when (article.category) {
                            HelpCategory.GETTING_STARTED -> Color(0xFF2196F3)
                            HelpCategory.SECURITY -> Color(0xFF4CAF50)
                            HelpCategory.FAMILY -> Color(0xFFFF9800)
                            HelpCategory.TROUBLESHOOTING -> Color(0xFFF44336)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = article.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VideoTutorialDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Видео-инструкция") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Плейсхолдер для видео
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Видео-руководство\nпо использованию приложения",
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = """
                        В этом видео вы узнаете:
                        • Как добавить членов семьи
                        • Как настроить уведомления
                        • Как управлять лимитами
                        • Что делать при тревожных сигналах
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}
