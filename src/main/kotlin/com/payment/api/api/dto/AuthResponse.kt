package com.payment.api.api.dto

import java.util.UUID

data class AuthResponse(
    val token: String,
    val userId: UUID,
    val email: String,
    val accountId: UUID
)
