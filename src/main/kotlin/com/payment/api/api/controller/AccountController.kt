package com.payment.api.api.controller

import com.payment.api.api.dto.AccountResponse
import com.payment.api.api.dto.CreateAccountRequest
import com.payment.api.api.dto.DepositRequest
import com.payment.api.application.usecase.CreateAccountUseCase
import com.payment.api.application.usecase.DepositUseCase
import com.payment.api.domain.repository.AccountRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val createAccountUseCase: CreateAccountUseCase,
    private val depositUseCase: DepositUseCase,
    private val accountRepository: AccountRepository
) {
    @PostMapping
    fun create(@Valid @RequestBody request: CreateAccountRequest): ResponseEntity<AccountResponse> {
        val account = createAccountUseCase.execute(
            agencia = request.agencia,
            numeroConta = request.numeroConta,
            titularNome = request.titularNome,
            cpf = request.cpf
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(account))
    }

    @PostMapping("/{id}/deposit")
    fun deposit(
        @PathVariable id: UUID,
        @Valid @RequestBody request: DepositRequest
    ): ResponseEntity<AccountResponse> {
        val account = depositUseCase.execute(id, request.amount)
        return ResponseEntity.ok(AccountResponse.from(account))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<AccountResponse> {
        val account = accountRepository.findById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(AccountResponse.from(account))
    }
}
