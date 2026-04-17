package com.payment.api.api.dto

import com.payment.api.domain.model.Transaction
import com.payment.api.domain.model.TransactionStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val idempotencyKey: String,
    val payerId: UUID,
    val receiverId: UUID,
    val amount: BigDecimal,
    val status: TransactionStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(transaction: Transaction) = TransactionResponse(
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
