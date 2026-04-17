package com.payment.api.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAccountRequest(
    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroConta: String,

    @field:NotBlank
    val titularNome: String,

    @field:NotBlank
    @field:Size(min = 11, max = 11)
    val cpf: String
)
