package com.payment.api.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Transaction(
    val id: UUID = UUID.randomUUID(),
    val idempotencyKey: String,
    val payerId: UUID,
    val receiverId: UUID,
    val amount: BigDecimal,
    val status: TransactionStatus = TransactionStatus.PENDING,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    fun transitionTo(newStatus: TransactionStatus): Transaction {
        require(status.canTransitionTo(newStatus)) {
            "Transição inválida: $status → $newStatus"
        }
        return copy(status = newStatus, updatedAt = Instant.now())
    }
}
