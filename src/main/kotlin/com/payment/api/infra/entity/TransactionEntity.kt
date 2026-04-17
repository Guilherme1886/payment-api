package com.payment.api.infra.entity

import com.payment.api.domain.model.Transaction
import com.payment.api.domain.model.TransactionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    val id: UUID,

    @Column(name = "idempotency_key", nullable = false, unique = true)
    val idempotencyKey: String,

    @Column(name = "payer_id", nullable = false)
    val payerId: UUID,

    @Column(name = "receiver_id", nullable = false)
    val receiverId: UUID,

    @Column(nullable = false, precision = 19, scale = 2)
    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: TransactionStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant
) {
    fun toDomain(): Transaction = Transaction(
        id = id,
        idempotencyKey = idempotencyKey,
        payerId = payerId,
        receiverId = receiverId,
        amount = amount,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(transaction: Transaction): TransactionEntity = TransactionEntity(
            id = transaction.id,
            idempotencyKey = transaction.idempotencyKey,
            payerId = transaction.payerId,
            receiverId = transaction.receiverId,
            amount = transaction.amount,
            status = transaction.status,
            createdAt = transaction.createdAt,
            updatedAt = transaction.updatedAt
        )
    }
}
