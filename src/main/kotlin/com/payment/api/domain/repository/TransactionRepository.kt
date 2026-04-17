package com.payment.api.domain.repository

import com.payment.api.domain.model.Transaction
import java.util.UUID

interface TransactionRepository {
    fun findById(id: UUID): Transaction?
    fun findByIdempotencyKey(idempotencyKey: String): Transaction?
    fun save(transaction: Transaction): Transaction
}
