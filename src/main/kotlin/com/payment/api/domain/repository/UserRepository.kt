package com.payment.api.domain.repository

import com.payment.api.domain.model.User
import java.util.UUID

interface UserRepository {
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun save(user: User): User
}
