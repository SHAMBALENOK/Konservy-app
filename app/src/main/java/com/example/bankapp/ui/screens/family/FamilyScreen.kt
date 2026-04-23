package com.example.bankapp.ui.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bankapp.data.model.*
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.theme.SuccessGreen
import com.example.bankapp.ui.theme.TextSecondary
import com.example.bankapp.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen(
    repository: FamilyRepository = remember { FamilyRepository() }
) {
    val familyMembers by repository.familyMembers.collectAsState()
    val suspiciousOperations by repository.suspiciousOperations.collectAsState()
    val notifications by repository.notifications.collectAsState()

    var selectedMember by remember { mutableStateOf<FamilyMember?>(null) }
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showSecuritySettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Семья и безопасность") },
                actions = {
                    IconButton(onClick = { showSecuritySettings = true }) {
                        Icon(Icons.Outlined.Security, contentDescription = "Настройки безопасности")
                    }
                    IconButton(onClick = { showAddMemberDialog = true }) {
                        Icon(Icons.Outlined.PersonAdd, contentDescription = "Добавить члена семьи")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Статистика
            item {
                Spacer(modifier = Modifier.height(16.dp))
                FamilyStatsCard(
                    totalMembers = familyMembers.size,
                    monitoredCount = familyMembers.count { it.isMonitored },
                    activeAlerts = suspiciousOperations.count { !it.isConfirmed && !it.isBlocked },
                    pendingNotifications = notifications.count { it.actionTaken == null }
                )
            }

            // Члены семьи
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Члены семьи",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(familyMembers) { member ->
                FamilyMemberCard(
                    member = member,
                    accounts = repository.getAccountsForMember(member.id),
                    onClick = { selectedMember = member },
                    onToggleMonitoring = {
                        repository.updateFamilyMember(member.copy(isMonitored = !member.isMonitored))
                    }
                )
            }

            // Подозрительные операции
            if (suspiciousOperations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Подозрительные операции",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(suspiciousOperations) { operation ->
                    SuspiciousOperationCard(
                        operation = operation,
                        onConfirm = { repository.confirmOperation(operation.id) },
                        onBlock = { repository.blockOperation(operation.id) }
                    )
                }
            }

            // Уведомления
            if (notifications.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Уведомления доверенным лицам",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(notifications) { notification ->
                    NotificationCard(notification = notification)
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Диалоги
    if (showAddMemberDialog) {
        AddMemberDialog(
            onDismiss = { showAddMemberDialog = false },
            onAddMember = { member ->
                repository.addFamilyMember(member)
                showAddMemberDialog = false
            }
        )
    }

    if (showSecuritySettings) {
        SecuritySettingsDialog(
            settings = repository.securitySettings.value,
            onDismiss = { showSecuritySettings = false },
            onSave = { settings ->
                repository.updateSecuritySettings(settings)
                showSecuritySettings = false
            }
        )
    }
}

@Composable
fun FamilyStatsCard(
    totalMembers: Int,
    monitoredCount: Int,
    activeAlerts: Int,
    pendingNotifications: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Обзор безопасности",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.People,
                    value = totalMembers.toString(),
                    label = "Членов семьи",
                    color = MaterialTheme.colorScheme.primary
                )
                StatItem(
                    icon = Icons.Outlined.Visibility,
                    value = monitoredCount.toString(),
                    label = "Под контролем",
                    color = MaterialTheme.colorScheme.secondary
                )
                StatItem(
                    icon = Icons.Outlined.Warning,
                    value = activeAlerts.toString(),
                    label = "Тревоги",
                    color = if (activeAlerts > 0) WarningOrange else SuccessGreen
                )
                StatItem(
                    icon = Icons.Outlined.Notifications,
                    value = pendingNotifications.toString(),
                    label = "Ожидают",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun FamilyMemberCard(
    member: FamilyMember,
    accounts: List<ExtendedAccount>,
    onClick: () -> Unit,
    onToggleMonitoring: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (member.relation) {
                                    "Я" -> Icons.Outlined.Person
                                    "Супруга", "Супруг" -> Icons.Outlined.Favorite
                                    "Сын", "Дочь" -> Icons.Outlined.ChildCare
                                    "Мать", "Отец" -> Icons.Outlined.Accessibility
                                    else -> Icons.Outlined.Person
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = member.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${member.relation}, ${member.age} лет",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = member.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TrustLevelBadge(level = member.trustLevel)
                    Switch(
                        checked = member.isMonitored,
                        onCheckedChange = { onToggleMonitoring() }
                    )
                }
            }

            // Счета
            if (accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        Surface(
                            modifier = Modifier
                                .width(100.dp)
                                .height(60.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = account.cardColor.copy(alpha = 0.2f)
                        ) {
                            Box(
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.BottomStart
                            ) {
                                Text(
                                    text = "${String.format("%,.0f", account.balance)} ₽",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = account.cardColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrustLevelBadge(level: TrustLevel) {
    val (color, text) = when (level) {
        TrustLevel.FULL -> SuccessGreen to "Полный"
        TrustLevel.STANDARD -> WarningOrange to "Стандарт"
        TrustLevel.RESTRICTED -> Color.Red to "Ограничен"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun SuspiciousOperationCard(
    operation: SuspiciousOperation,
    onConfirm: () -> Unit,
    onBlock: () -> Unit
) {
    val riskColor = when (operation.riskLevel) {
        RiskLevel.LOW -> SuccessGreen
        RiskLevel.MEDIUM -> WarningOrange
        RiskLevel.HIGH -> Color.Red
        RiskLevel.CRITICAL -> Color(0xFF7B1FA2)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (operation.type) {
                            OperationType.TRANSFER -> Icons.Outlined.SwapHoriz
                            OperationType.PAYMENT -> Icons.Outlined.Receipt
                            OperationType.WITHDRAWAL -> Icons.Outlined.Money
                            OperationType.PHONE_CALL -> Icons.Outlined.Phone
                            OperationType.MESSENGER_ACTIVITY -> Icons.Outlined.Message
                            else -> Icons.Outlined.Warning
                        },
                        contentDescription = null,
                        tint = riskColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = operation.memberName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = operation.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = riskColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = when (operation.riskLevel) {
                            RiskLevel.LOW -> "Низкий"
                            RiskLevel.MEDIUM -> "Средний"
                            RiskLevel.HIGH -> "Высокий"
                            RiskLevel.CRITICAL -> "Критический"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = riskColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (operation.amount > 0) {
                Text(
                    text = "Сумма: ${String.format("%,.2f", operation.amount)} ₽",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!operation.isConfirmed && !operation.isBlocked) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = SuccessGreen
                        )
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Подтвердить")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onBlock,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Outlined.Block, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Заблокировать")
                    }
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (operation.isConfirmed) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Подтверждено",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                    if (operation.isBlocked) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Red.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.Block,
                                    contentDescription = null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Заблокировано",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: FamilyNotification) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Уведомление отправлено ${notification.sentTo.size} контактам",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = notification.operation.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Подтвердили:",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                if (notification.acknowledgedBy.isEmpty()) {
                    Text(
                        text = "Пока никто",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarningOrange
                    )
                } else {
                    notification.acknowledgedBy.forEach { phone ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = phone.takeLast(4),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (notification.actionTaken != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (notification.actionTaken) {
                        NotificationAction.BLOCKED -> Color.Red.copy(alpha = 0.2f)
                        NotificationAction.CONFIRMED -> SuccessGreen.copy(alpha = 0.2f)
                        NotificationAction.IGNORED -> WarningOrange.copy(alpha = 0.2f)
                        NotificationAction.ESCALATED -> Color(0xFF7B1FA2).copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (notification.actionTaken) {
                            NotificationAction.BLOCKED -> "Операция заблокирована"
                            NotificationAction.CONFIRMED -> "Операция подтверждена"
                            NotificationAction.IGNORED -> "Проигнорировано"
                            NotificationAction.ESCALATED -> "Эскалировано"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when (notification.actionTaken) {
                            NotificationAction.BLOCKED -> Color.Red
                            NotificationAction.CONFIRMED -> SuccessGreen
                            NotificationAction.IGNORED -> WarningOrange
                            NotificationAction.ESCALATED -> Color(0xFF7B1FA2)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onAddMember: (FamilyMember) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("Ребёнок") }
    var age by remember { mutableStateOf("18") }
    var phone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить члена семьи") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ФИО") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Using a simple TextField instead of ExposedDropdownMenuBox for now
                OutlinedTextField(
                    value = relation,
                    onValueChange = { relation = it },
                    label = { Text("Родственная связь") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Возраст") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Телефон") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddMember(
                        FamilyMember(
                            id = System.currentTimeMillis().toInt(),
                            name = name,
                            relation = relation,
                            age = age.toIntOrNull() ?: 18,
                            phone = phone,
                            accounts = emptyList(),
                            isMonitored = true,
                            trustLevel = TrustLevel.STANDARD
                        )
                    )
                },
                enabled = name.isNotBlank() && phone.isNotBlank()
            ) {
                Text("Добавить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun SecuritySettingsDialog(
    settings: SecuritySettings,
    onDismiss: () -> Unit,
    onSave: (SecuritySettings) -> Unit
) {
    var maxTransfer by remember { mutableStateOf(settings.maxTransferAmount.toString()) }
    var maxWithdrawal by remember { mutableStateOf(settings.maxDailyWithdrawal.toString()) }
    var maxCallDuration by remember { mutableStateOf(settings.maxCallDurationMinutes.toString()) }
    var notifyAll by remember { mutableStateOf(settings.notifyAllContacts) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки безопасности") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = maxTransfer,
                    onValueChange = { maxTransfer = it },
                    label = { Text("Макс. перевод (₽)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxWithdrawal,
                    onValueChange = { maxWithdrawal = it },
                    label = { Text("Макс. снятие за день (₽)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxCallDuration,
                    onValueChange = { maxCallDuration = it },
                    label = { Text("Макс. длительность звонка (мин)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Уведомлять всех контактов")
                    Switch(checked = notifyAll, onCheckedChange = { notifyAll = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        SecuritySettings(
                            maxTransferAmount = maxTransfer.toDoubleOrNull() ?: 50000.0,
                            maxDailyWithdrawal = maxWithdrawal.toDoubleOrNull() ?: 100000.0,
                            maxCallDurationMinutes = maxCallDuration.toIntOrNull() ?: 60,
                            notifyAllContacts = notifyAll
                        )
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
