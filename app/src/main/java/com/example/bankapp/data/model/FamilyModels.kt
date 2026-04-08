package com.example.bankapp.data.model

import androidx.compose.ui.graphics.Color

/**
 * Модель доверенного лица ("Вторая рука")
 */
data class TrustedContact(
    val id: Int,
    val name: String,
    val phone: String,
    val relationship: String, // "Супруг(а)", "Ребёнок", "Родитель" и т.д.
    val isActive: Boolean = true,
    val notifyOnAllOperations: Boolean = false,
    val notifyOnSuspiciousOnly: Boolean = true
)

/**
 * Модель члена семьи для контроля
 */
data class FamilyMember(
    val id: Int,
    val name: String,
    val relation: String, // "Я", "Супруг(а)", "Ребёнок", "Родитель"
    val age: Int,
    val phone: String,
    val accounts: List<String>, // ID счетов
    val isMonitored: Boolean,
    val trustLevel: TrustLevel
)

enum class TrustLevel {
    FULL,      // Полный доступ без ограничений
    STANDARD,  // Стандартные лимиты
    RESTRICTED // Ограниченный доступ с уведомлениями
}

/**
 * Модель подозрительной операции
 */
data class SuspiciousOperation(
    val id: Int,
    val memberId: Int,
    val memberName: String,
    val type: OperationType,
    val amount: Double,
    val recipient: String?,
    val timestamp: Long,
    val riskLevel: RiskLevel,
    val description: String,
    val isConfirmed: Boolean = false,
    val isBlocked: Boolean = false
)

enum class OperationType {
    TRANSFER,           // Перевод
    PAYMENT,            // Оплата
    WITHDRAWAL,         // Снятие наличных
    ONLINE_PURCHASE,    // Покупка онлайн
    PHONE_CALL,         // Длительный звонок
    MESSENGER_ACTIVITY  // Активность в мессенджере
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

/**
 * Настройки уведомлений и лимитов
 */
data class SecuritySettings(
    val maxTransferAmount: Double = 50000.0,
    val maxDailyWithdrawal: Double = 100000.0,
    val maxCallDurationMinutes: Int = 60,
    val continuousMessengerMinutes: Int = 30,
    val notifyAllContacts: Boolean = true,
    val blockOnCriticalRisk: Boolean = true,
    val requireConfirmationFor: Set<OperationType> = setOf(
        OperationType.TRANSFER,
        OperationType.WITHDRAWAL
    )
)

/**
 * Модель счёта с расширенной информацией
 */
data class ExtendedAccount(
    val id: Int,
    val accountName: String,
    val accountNumber: String,
    val balance: Double,
    val currency: String,
    val cardColor: Color,
    val ownerMemberId: Int,
    val isJoint: Boolean = false,
    val spendingLimit: Double? = null
)

/**
 * Уведомление для членов семьи
 */
data class FamilyNotification(
    val id: Int,
    val operation: SuspiciousOperation,
    val sentTo: List<String>, // Телефоны получателей
    val sentAt: Long,
    val acknowledgedBy: List<String>, // Кто подтвердил
    val actionTaken: NotificationAction?
)

enum class NotificationAction {
    BLOCKED,
    CONFIRMED,
    IGNORED,
    ESCALATED
}
