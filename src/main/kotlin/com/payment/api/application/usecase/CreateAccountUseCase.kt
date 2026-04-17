package com.payment.api.application.usecase

import com.payment.api.domain.model.Account
import com.payment.api.domain.repository.AccountRepository
import org.springframework.stereotype.Service

@Service
class CreateAccountUseCase(
    private val accountRepository: AccountRepository
) {
    fun execute(agencia: String, numeroConta: String, titularNome: String, cpf: String): Account {
        accountRepository.findByCpf(cpf)?.let {
            throw IllegalArgumentException("Conta já existente para o CPF informado")
        }
        val account = Account(
            agencia = agencia,
            numeroConta = numeroConta,
            titularNome = titularNome,
            cpf = cpf
        )
        return accountRepository.save(account)
    }
}
