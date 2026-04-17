package com.payment.api.application.usecase

import com.payment.api.domain.model.Account
import com.payment.api.domain.model.User
import com.payment.api.domain.repository.AccountRepository
import com.payment.api.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUseCase(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun execute(
        email: String,
        password: String,
        agencia: String,
        numeroConta: String,
        titularNome: String,
        cpf: String
    ): User {
        userRepository.findByEmail(email)?.let {
            throw IllegalArgumentException("Email já cadastrado")
        }
        accountRepository.findByCpf(cpf)?.let {
            throw IllegalArgumentException("Conta já existente para o CPF informado")
        }

        val account = accountRepository.save(
            Account(
                agencia = agencia,
                numeroConta = numeroConta,
                titularNome = titularNome,
                cpf = cpf
            )
        )

        return userRepository.save(
            User(
                email = email,
                password = passwordEncoder.encode(password),
                accountId = account.id
            )
        )
    }
}
