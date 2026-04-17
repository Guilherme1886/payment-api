package com.payment.api.infra.entity

import com.payment.api.domain.model.Account
import com.payment.api.domain.model.AccountStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "accounts")
class AccountEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    val agencia: String,

    @Column(name = "numero_conta", nullable = false)
    val numeroConta: String,

    @Column(name = "titular_nome", nullable = false)
    val titularNome: String,

    @Column(nullable = false, unique = true)
    val cpf: String,

    @Column(nullable = false, precision = 19, scale = 2)
    val balance: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: AccountStatus,

    @Column(nullable = false)
    val currency: String,

    @Version
    val version: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    fun toDomain(): Account = Account(
        id = id,
        agencia = agencia,
        numeroConta = numeroConta,
        titularNome = titularNome,
        cpf = cpf,
        balance = balance,
        status = status,
        currency = currency,
        version = version,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(account: Account): AccountEntity = AccountEntity(
            id = account.id,
            agencia = account.agencia,
            numeroConta = account.numeroConta,
            titularNome = account.titularNome,
            cpf = account.cpf,
            balance = account.balance,
            status = account.status,
            currency = account.currency,
            version = account.version,
            createdAt = account.createdAt
        )
    }
}
