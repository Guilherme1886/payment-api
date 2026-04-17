package com.payment.api.api.dto

import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal

data class DepositRequest(
    @field:DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    val amount: BigDecimal
)
