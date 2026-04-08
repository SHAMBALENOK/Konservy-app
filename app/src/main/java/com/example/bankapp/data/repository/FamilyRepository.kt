package com.example.bankapp.data.repository

import com.example.bankapp.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.ui.graphics.Color

/**
 * Репозиторий для управления семейными счетами и уведомлениями
 */
class FamilyRepository {

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    private val _trustedContacts = MutableStateFlow<List<TrustedContact>>(emptyList())
    val trustedContacts: StateFlow<List<TrustedContact>> = _trustedContacts.asStateFlow()

    private val _suspiciousOperations = MutableStateFlow<List<SuspiciousOperation>>(emptyList())
    val suspiciousOperations: StateFlow<List<SuspiciousOperation>> = _suspiciousOperations.asStateFlow()

    private val _securitySettings = MutableStateFlow(SecuritySettings())
    val securitySettings: StateFlow<SecuritySettings> = _securitySettings.asStateFlow()

    private val _notifications = MutableStateFlow<List<FamilyNotification>>(emptyList())
    val notifications: StateFlow<List<FamilyNotification>> = _notifications.asStateFlow()

    init {
        // Инициализация тестовыми данными
        loadSampleData()
    }

    private fun loadSampleData() {
        _familyMembers.value = listOf(
            FamilyMember(
                id = 1,
                name = "Александр Петров",
                relation = "Я",
                age = 45,
                phone = "+7 (999) 123-45-67",
                accounts = listOf("1", "2"),
                isMonitored = false,
                trustLevel = TrustLevel.FULL
            ),
            FamilyMember(
                id = 2,
                name = "Мария Петрова",
                relation = "Супруга",
                age = 42,
                phone = "+7 (999) 123-45-68",
                accounts = listOf("3"),
                isMonitored = true,
                trustLevel = TrustLevel.STANDARD
            ),
            FamilyMember(
                id = 3,
                name = "Дмитрий Петров",
                relation = "Сын",
                age = 17,
                phone = "+7 (999) 123-45-69",
                accounts = listOf("4"),
                isMonitored = true,
                trustLevel = TrustLevel.RESTRICTED
            ),
            FamilyMember(
                id = 4,
                name = "Елена Иванова",
                relation = "Мать",
                age = 72,
                phone = "+7 (999) 123-45-70",
                accounts = listOf("5"),
                isMonitored = true,
                trustLevel = TrustLevel.RESTRICTED
            )
        )

        _trustedContacts.value = listOf(
            TrustedContact(
                id = 1,
                name = "Мария Петрова",
                phone = "+7 (999) 123-45-68",
                relationship = "Супруга",
                isActive = true,
                notifyOnAllOperations = true,
                notifyOnSuspiciousOnly = false
            ),
            TrustedContact(
                id = 2,
                name = "Анна Смирнова",
                phone = "+7 (999) 123-45-71",
                relationship = "Дочь",
                isActive = true,
                notifyOnAllOperations = false,
                notifyOnSuspiciousOnly = true
            )
        )

        _suspiciousOperations.value = listOf(
            SuspiciousOperation(
                id = 1,
                memberId = 3,
                memberName = "Дмитрий Петров",
                type = OperationType.TRANSFER,
                amount = 15000.0,
                recipient = "+7 (900) XXX-XX-XX",
                timestamp = System.currentTimeMillis() - 3600000,
                riskLevel = RiskLevel.MEDIUM,
                description = "Перевод на незнакомый номер"
            ),
            SuspiciousOperation(
                id = 2,
                memberId = 4,
                memberName = "Елена Иванова",
                type = OperationType.PHONE_CALL,
                amount = 0.0,
                recipient = "+7 (495) XXX-XX-XX",
                timestamp = System.currentTimeMillis() - 7200000,
                riskLevel = RiskLevel.HIGH,
                description = "Длительный разговор с незнакомцем (1ч 25мин)"
            )
        )
    }

    fun addFamilyMember(member: FamilyMember) {
        _familyMembers.value = _familyMembers.value + member
    }

    fun updateFamilyMember(member: FamilyMember) {
        _familyMembers.value = _familyMembers.value.map {
            if (it.id == member.id) member else it
        }
    }

    fun removeFamilyMember(memberId: Int) {
        _familyMembers.value = _familyMembers.value.filter { it.id != memberId }
    }

    fun addTrustedContact(contact: TrustedContact) {
        _trustedContacts.value = _trustedContacts.value + contact
    }

    fun updateTrustedContact(contact: TrustedContact) {
        _trustedContacts.value = _trustedContacts.value.map {
            if (it.id == contact.id) contact else it
        }
    }

    fun removeTrustedContact(contactId: Int) {
        _trustedContacts.value = _trustedContacts.value.filter { it.id != contactId }
    }

    fun addSuspiciousOperation(operation: SuspiciousOperation) {
        _suspiciousOperations.value = listOf(operation) + _suspiciousOperations.value
        // Автоматически отправить уведомления всем доверенным контактам
        sendNotificationToAll(operation)
    }

    fun confirmOperation(operationId: Int) {
        _suspiciousOperations.value = _suspiciousOperations.value.map {
            if (it.id == operationId) it.copy(isConfirmed = true) else it
        }
    }

    fun blockOperation(operationId: Int) {
        _suspiciousOperations.value = _suspiciousOperations.value.map {
            if (it.id == operationId) it.copy(isBlocked = true) else it
        }
    }

    fun updateSecuritySettings(settings: SecuritySettings) {
        _securitySettings.value = settings
    }

    private fun sendNotificationToAll(operation: SuspiciousOperation) {
        val activeContacts = _trustedContacts.value.filter { it.isActive }
        val phones = activeContacts.map { it.phone }

        val notification = FamilyNotification(
            id = System.currentTimeMillis().toInt(),
            operation = operation,
            sentTo = phones,
            sentAt = System.currentTimeMillis(),
            acknowledgedBy = emptyList(),
            actionTaken = null
        )

        _notifications.value = listOf(notification) + _notifications.value
    }

    fun acknowledgeNotification(notificationId: Int, phone: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId && phone !in it.acknowledgedBy) {
                it.copy(acknowledgedBy = it.acknowledgedBy + phone)
            } else {
                it
            }
        }
    }

    fun takeActionOnNotification(notificationId: Int, action: NotificationAction) {
        _notifications.value = _notifications.value.map {
            if (it.id == notificationId) it.copy(actionTaken = action) else it
        }

        if (action == NotificationAction.BLOCKED) {
            val notification = _notifications.value.find { it.id == notificationId }
            notification?.let {
                blockOperation(it.operation.id)
            }
        }
    }

    fun checkOperationRisk(
        type: OperationType,
        amount: Double,
        durationMinutes: Int? = null
    ): RiskLevel {
        val settings = _securitySettings.value

        return when (type) {
            OperationType.TRANSFER -> {
                when {
                    amount > settings.maxTransferAmount * 2 -> RiskLevel.CRITICAL
                    amount > settings.maxTransferAmount -> RiskLevel.HIGH
                    amount > settings.maxTransferAmount * 0.5 -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
            }
            OperationType.WITHDRAWAL -> {
                when {
                    amount > settings.maxDailyWithdrawal -> RiskLevel.HIGH
                    amount > settings.maxDailyWithdrawal * 0.5 -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
            }
            OperationType.PHONE_CALL, OperationType.MESSENGER_ACTIVITY -> {
                durationMinutes?.let {
                    when {
                        it > settings.maxCallDurationMinutes * 2 -> RiskLevel.CRITICAL
                        it > settings.maxCallDurationMinutes -> RiskLevel.HIGH
                        it > settings.maxCallDurationMinutes * 0.5 -> RiskLevel.MEDIUM
                        else -> RiskLevel.LOW
                    }
                } ?: RiskLevel.LOW
            }
            else -> RiskLevel.LOW
        }
    }

    fun getAccountsForMember(memberId: Int): List<ExtendedAccount> {
        val member = _familyMembers.value.find { it.id == memberId } ?: return emptyList()
        
        return member.accounts.mapIndexed { index, accountId ->
            ExtendedAccount(
                id = accountId.toIntOrNull() ?: index,
                accountName = "${member.relation} - Счёт ${index + 1}",
                accountNumber = "**** ${1000 + index}",
                balance = 50000.0 * (index + 1),
                currency = "RUB",
                cardColor = when (index % 3) {
                    0 -> Color(0xFF1976D2)
                    1 -> Color(0xFF00897B)
                    else -> Color(0xFFE91E63)
                },
                ownerMemberId = memberId,
                isJoint = member.relation != "Я",
                spendingLimit = when (member.trustLevel) {
                    TrustLevel.FULL -> null
                    TrustLevel.STANDARD -> 50000.0
                    TrustLevel.RESTRICTED -> 10000.0
                }
            )
        }
    }
}
