package com.payment.api.infra.repository

import com.payment.api.infra.entity.TransactionEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface JpaTransactionRepository : JpaRepository<TransactionEntity, UUID> {
    fun findByIdempotencyKey(idempotencyKey: String): TransactionEntity?
}
