package com.payment.api.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank
    @field:Email
    val email: String,

    @field:NotBlank
    @field:Size(min = 6)
    val password: String,

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
