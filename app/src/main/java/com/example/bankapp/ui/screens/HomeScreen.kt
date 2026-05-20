package com.example.bankapp.ui.screens

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
import com.example.bankapp.data.repository.FamilyRepository
import com.example.bankapp.ui.theme.SuccessGreen
import com.example.bankapp.ui.theme.TextSecondary
import com.example.bankapp.ui.theme.WarningOrange

data class AccountCard(
    val id: Int,
    val accountId: String,
    val accountName: String,
    val accountNumber: String,
    val balance: Double,
    val currency: String,
    val cardColor: Color
)

data class TransactionItem(
    val id: Int,
    val title: String,
    val date: String,
    val amount: Double,
    val isIncome: Boolean,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: FamilyRepository,
    onOpenSettings: () -> Unit,
    onDeposit: () -> Unit = {}
) {
    // Наблюдаем за состоянием счетов и именем пользователя
    val accountsState by repository.accounts.collectAsState()
    val username by repository.currentUsername.collectAsState()
    
    // Формируем список счетов для отображения
    val accounts = accountsState.map { account ->
        AccountCard(
            id = account.id,
            accountId = account.accountId.toString(),
            accountName = account.accountName,
            accountNumber = account.accountNumber,
            balance = account.balance,
            currency = account.currency,
            cardColor = account.cardColor
        )
    }
    
    // Заглушка для транзакций (будет загружаться с сервера)
    val transactions = listOf<TransactionItem>()
    
    // Получаем имя для отображения - используем имя пользователя
    val displayName = username.ifBlank { "Пользователь" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой Банк") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Настройки")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Поиск")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.People, contentDescription = null) },
                    label = { Text("Семья") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.SwapHoriz, contentDescription = null) },
                    label = { Text("Переводы") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                    label = { Text("Профиль") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Приветствие
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Добрый день, ${displayName}!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Вычисляем общий баланс
                val totalBalance = accounts.sumOf { it.balance }
                Text(
                    text = "Общий баланс: ${String.format("%,.2f", totalBalance)} ${accounts.firstOrNull()?.currency ?: "RUB"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Быстрые действия
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(icon = Icons.Outlined.Send, label = "Перевести", onClick = {})
                    QuickActionButton(icon = Icons.Outlined.Add, label = "Пополнить", onClick = onDeposit)
                    QuickActionButton(icon = Icons.Outlined.Receipt, label = "Платёж", onClick = {})
                    QuickActionButton(icon = Icons.Outlined.MoreHoriz, label = "Ещё", onClick = {})
                }
            }

            // Карты
            item {
                Text(
                    text = "Мои карты",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(accounts) { account ->
                AccountCardItem(account = account)
            }

            // Транзакции
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "История операций",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { }) {
                        Text("Все")
                    }
                }
            }

            items(transactions) { transaction ->
                TransactionItemRow(transaction = transaction)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
    }
}

@Composable
fun AccountCardItem(account: AccountCard) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp)),
        color = account.cardColor,
        shadowElevation = 8.dp,
        onClick = { }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = account.accountName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = account.accountNumber,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Баланс",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%,.2f", account.balance)} $account.currency",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Иконка чипа
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(40.dp, 30.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Color(0xFFFFD700)
            ) {}
        }
    }
}

@Composable
fun TransactionItemRow(transaction: TransactionItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        onClick = { }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        transaction.icon()
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = "${if (transaction.amount > 0) "+" else ""}${String.format("%,.2f", transaction.amount)} ₽",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.isIncome) SuccessGreen else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
