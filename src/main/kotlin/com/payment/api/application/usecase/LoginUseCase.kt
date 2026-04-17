package com.payment.api.application.usecase

import com.payment.api.domain.model.User
import com.payment.api.domain.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class LoginUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun execute(email: String, password: String): User {
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("Credenciais inválidas")

        if (!passwordEncoder.matches(password, user.password)) {
            throw IllegalArgumentException("Credenciais inválidas")
        }

        return user
    }
}
