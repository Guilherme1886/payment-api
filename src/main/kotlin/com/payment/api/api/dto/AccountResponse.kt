package com.payment.api.api.dto

import com.payment.api.domain.model.Account
import com.payment.api.domain.model.AccountStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AccountResponse(
    val id: UUID,
    val agencia: String,
    val numeroConta: String,
    val titularNome: String,
    val cpf: String,
    val balance: BigDecimal,
    val status: AccountStatus,
    val currency: String,
    val createdAt: Instant
) {
    companion object {
        fun from(account: Account) = AccountResponse(
            id = account.id,
            agencia = account.agencia,
            numeroConta = account.numeroConta,
            titularNome = account.titularNome,
            cpf = account.cpf,
            balance = account.balance,
            status = account.status,
            currency = account.currency,
            createdAt = account.createdAt
        )
    }
}
