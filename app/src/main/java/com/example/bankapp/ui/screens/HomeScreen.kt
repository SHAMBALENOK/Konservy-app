package com.example.bankapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    onOpenSettings: () -> Unit
) {
    val accounts = listOf(
        AccountCard(1, "Основной счёт", "**** 4521", 125430.50, "RUB", Color(0xFF1976D2)),
        AccountCard(2, "Сберегательный", "**** 8832", 500000.00, "RUB", Color(0xFF00897B)),
        AccountCard(3, "Кредитная карта", "**** 1234", -15000.00, "RUB", Color(0xFFE91E63))
    )

    val transactions = listOf(
        TransactionItem(1, "Перевод от Иванова А.", "Сегодня", 5000.0, true) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen)
        },
        TransactionItem(2, "Оплата ЖКХ", "Вчера", -3500.0, false) {
            Icon(Icons.Default.Home, contentDescription = null, tint = WarningOrange)
        },
        TransactionItem(3, "Покупка в магазине", "Вчера", -2150.0, false) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = TextSecondary)
        },
        TransactionItem(4, "Зарплата", "15 дек", 85000.0, true) {
            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = SuccessGreen)
        },
        TransactionItem(5, "Кафе \"Центральное\"", "14 дек", -1200.0, false) {
            Icon(Icons.Default.Restaurant, contentDescription = null, tint = TextSecondary)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мой Банк") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Уведомления")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Search, contentDescription = "Поиск")
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
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Главная") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    label = { Text("Платежи") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                    label = { Text("Переводы") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
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
                    text = "Добрый день, Александр!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Общий баланс: 610 430,50 ₽",
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
                    QuickActionButton(icon = Icons.Default.Send, label = "Перевести")
                    QuickActionButton(icon = Icons.Default.Add, label = "Пополнить")
                    QuickActionButton(icon = Icons.Default.Receipt, label = "Платёж")
                    QuickActionButton(icon = Icons.Default.MoreHoriz, label = "Ещё")
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
fun QuickActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Surface(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = { }
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
