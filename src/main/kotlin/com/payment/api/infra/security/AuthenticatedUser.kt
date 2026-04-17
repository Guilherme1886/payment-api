package com.payment.api.infra.security

import java.util.UUID

data class AuthenticatedUser(
    val userId: UUID,
    val email: String,
    val accountId: UUID
)
