package com.payment.api.application.usecase

import com.payment.api.domain.model.Account
import com.payment.api.domain.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class DepositUseCase(
    private val accountRepository: AccountRepository
) {
    @Transactional
    fun execute(accountId: UUID, amount: BigDecimal): Account {
        require(amount > BigDecimal.ZERO) { "Valor deve ser positivo" }

        val account = accountRepository.findByIdForUpdate(accountId)
            ?: throw IllegalArgumentException("Conta não encontrada: $accountId")

        require(account.isActive()) { "Conta não está ativa" }

        return accountRepository.save(account.credit(amount))
    }
}
