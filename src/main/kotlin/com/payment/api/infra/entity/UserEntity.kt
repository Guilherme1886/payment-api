package com.payment.api.infra.entity

import com.payment.api.domain.model.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    val password: String,

    @Column(name = "account_id", nullable = false)
    val accountId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    fun toDomain(): User = User(
        id = id,
        email = email,
        password = password,
        accountId = accountId,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(user: User): UserEntity = UserEntity(
            id = user.id,
            email = user.email,
            password = user.password,
            accountId = user.accountId,
            createdAt = user.createdAt
        )
    }
}
