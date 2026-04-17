package com.payment.api.api.controller

import com.payment.api.api.dto.AuthResponse
import com.payment.api.api.dto.LoginRequest
import com.payment.api.api.dto.RegisterRequest
import com.payment.api.application.usecase.LoginUseCase
import com.payment.api.application.usecase.RegisterUseCase
import com.payment.api.infra.security.JwtTokenProvider
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        val user = registerUseCase.execute(
            email = request.email,
            password = request.password,
            agencia = request.agencia,
            numeroConta = request.numeroConta,
            titularNome = request.titularNome,
            cpf = request.cpf
        )
        val token = jwtTokenProvider.generateToken(
            userId = user.id.toString(),
            email = user.email,
            accountId = user.accountId.toString()
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            AuthResponse(
                token = token,
                userId = user.id,
                email = user.email,
                accountId = user.accountId
            )
        )
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<AuthResponse> {
        val user = loginUseCase.execute(request.email, request.password)
        val token = jwtTokenProvider.generateToken(
            userId = user.id.toString(),
            email = user.email,
            accountId = user.accountId.toString()
        )
        return ResponseEntity.ok(
            AuthResponse(
                token = token,
                userId = user.id,
                email = user.email,
                accountId = user.accountId
            )
        )
    }
}
