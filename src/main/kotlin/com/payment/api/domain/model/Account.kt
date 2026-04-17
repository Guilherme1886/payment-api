package com.payment.api.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Account(
    val id: UUID = UUID.randomUUID(),
    val agencia: String,
    val numeroConta: String,
    val titularNome: String,
    val cpf: String,
    val balance: BigDecimal = BigDecimal.ZERO,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val currency: String = "BRL",
    val version: Long = 0,
    val createdAt: Instant = Instant.now()
) {
    fun debit(amount: BigDecimal): Account {
        require(balance >= amount) { "Saldo insuficiente" }
        return copy(balance = balance - amount)
    }

    fun credit(amount: BigDecimal): Account =
        copy(balance = balance + amount)

    fun isActive(): Boolean = status == AccountStatus.ACTIVE
}
