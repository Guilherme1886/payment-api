package com.payment.api.domain.model

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val password: String,
    val accountId: UUID,
    val createdAt: Instant = Instant.now()
)
