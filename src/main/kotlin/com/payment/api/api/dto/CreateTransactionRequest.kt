package com.payment.api.api.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class CreateTransactionRequest(
    @field:NotBlank
    val idempotencyKey: String,

    @field:NotNull
    val payerId: UUID,

    @field:NotNull
    val receiverId: UUID,

    @field:NotNull
    @field:DecimalMin("0.01")
    val amount: BigDecimal
)
