package com.payment.api.infra.repository

import com.payment.api.domain.model.User
import com.payment.api.domain.repository.UserRepository
import com.payment.api.infra.entity.UserEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserRepositoryImpl(
    private val jpa: JpaUserRepository
) : UserRepository {

    override fun findById(id: UUID): User? =
        jpa.findByIdOrNull(id)?.toDomain()

    override fun findByEmail(email: String): User? =
        jpa.findByEmail(email)?.toDomain()

    override fun save(user: User): User =
        jpa.save(UserEntity.fromDomain(user)).toDomain()
}
