package com.payment.api.infra.repository

import com.payment.api.infra.entity.AccountEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface JpaAccountRepository : JpaRepository<AccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.id = :id")
    fun findByIdForUpdate(id: UUID): AccountEntity?

    fun findByCpf(cpf: String): AccountEntity?
}
