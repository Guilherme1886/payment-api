package com.payment.api.application.usecase

import com.payment.api.domain.exception.ForbiddenException
import com.payment.api.domain.exception.InsufficientFundsException
import com.payment.api.domain.model.Transaction
import com.payment.api.domain.model.TransactionStatus
import com.payment.api.domain.repository.AccountRepository
import com.payment.api.domain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

@Service
class ProcessPaymentUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {
    @Transactional
    fun execute(authenticatedAccountId: UUID, idempotencyKey: String, payerId: UUID, receiverId: UUID, amount: BigDecimal): Transaction {
        transactionRepository.findByIdempotencyKey(idempotencyKey)?.let { return it }

        if (payerId != authenticatedAccountId) {
            throw ForbiddenException("You can only transfer from your own account")
        }
        if (payerId == receiverId) {
            throw InsufficientFundsException("Cannot transfer to the same account")
        }
        require(amount > BigDecimal.ZERO) { "Valor deve ser positivo" }

        // Lock ordered by ID to prevent deadlocks
        val (firstId, secondId) = if (payerId < receiverId) payerId to receiverId else receiverId to payerId
        val first = accountRepository.findByIdForUpdate(firstId)
            ?: throw IllegalArgumentException("Conta não encontrada: $firstId")
        val second = accountRepository.findByIdForUpdate(secondId)
            ?: throw IllegalArgumentException("Conta não encontrada: $secondId")

        val payer = if (firstId == payerId) first else second
        val receiver = if (firstId == receiverId) first else second

        require(payer.isActive()) { "Conta do pagador não está ativa" }
        require(receiver.isActive()) { "Conta do recebedor não está ativa" }

        if (payer.balance < amount) {
            throw InsufficientFundsException("Insufficient funds")
        }

        val transaction = transactionRepository.save(
            Transaction(
                idempotencyKey = idempotencyKey,
                payerId = payerId,
                receiverId = receiverId,
                amount = amount,
                status = TransactionStatus.PENDING
            )
        )

        val processing = transaction.transitionTo(TransactionStatus.PROCESSING)
        transactionRepository.save(processing)

        accountRepository.save(payer.debit(amount))
        accountRepository.save(receiver.credit(amount))

        return transactionRepository.save(processing.transitionTo(TransactionStatus.COMPLETED))
    }
}
