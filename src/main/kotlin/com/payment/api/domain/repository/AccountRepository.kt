package com.payment.api.domain.repository

import com.payment.api.domain.model.Account
import java.util.UUID

interface AccountRepository {
    fun findById(id: UUID): Account?
    fun findByIdForUpdate(id: UUID): Account?
    fun findByCpf(cpf: String): Account?
    fun save(account: Account): Account
}
