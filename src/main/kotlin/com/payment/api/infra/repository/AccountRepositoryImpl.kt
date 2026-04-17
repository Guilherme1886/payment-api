package com.payment.api.infra.repository

import com.payment.api.domain.model.Account
import com.payment.api.domain.repository.AccountRepository
import com.payment.api.infra.entity.AccountEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class AccountRepositoryImpl(
    private val jpa: JpaAccountRepository
) : AccountRepository {

    override fun findById(id: UUID): Account? =
        jpa.findByIdOrNull(id)?.toDomain()

    override fun findByIdForUpdate(id: UUID): Account? =
        jpa.findByIdForUpdate(id)?.toDomain()

    override fun findByCpf(cpf: String): Account? =
        jpa.findByCpf(cpf)?.toDomain()

    override fun save(account: Account): Account =
        jpa.save(AccountEntity.fromDomain(account)).toDomain()
}
