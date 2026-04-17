package com.payment.api.api.controller

import com.payment.api.api.dto.CreateTransactionRequest
import com.payment.api.api.dto.TransactionResponse
import com.payment.api.application.usecase.ProcessPaymentUseCase
import com.payment.api.domain.repository.TransactionRepository
import com.payment.api.infra.security.AuthenticatedUser
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/transactions")
class TransactionController(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val transactionRepository: TransactionRepository
) {
    @PostMapping
    fun create(
        @AuthenticationPrincipal user: AuthenticatedUser,
        @Valid @RequestBody request: CreateTransactionRequest
    ): ResponseEntity<TransactionResponse> {
        val transaction = processPaymentUseCase.execute(
            authenticatedAccountId = user.accountId,
            idempotencyKey = request.idempotencyKey,
            payerId = request.payerId,
            receiverId = request.receiverId,
            amount = request.amount
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.from(transaction))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<TransactionResponse> {
        val transaction = transactionRepository.findById(id)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TransactionResponse.from(transaction))
    }
}
