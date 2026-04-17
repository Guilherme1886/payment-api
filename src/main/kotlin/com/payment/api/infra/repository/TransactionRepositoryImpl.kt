package com.payment.api.infra.repository

import com.payment.api.domain.model.Transaction
import com.payment.api.domain.repository.TransactionRepository
import com.payment.api.infra.entity.TransactionEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class TransactionRepositoryImpl(
    private val jpa: JpaTransactionRepository
) : TransactionRepository {

    override fun findById(id: UUID): Transaction? =
        jpa.findByIdOrNull(id)?.toDomain()

    override fun findByIdempotencyKey(idempotencyKey: String): Transaction? =
        jpa.findByIdempotencyKey(idempotencyKey)?.toDomain()

    override fun save(transaction: Transaction): Transaction =
        jpa.save(TransactionEntity.fromDomain(transaction)).toDomain()
}
