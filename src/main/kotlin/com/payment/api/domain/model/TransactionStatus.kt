package com.payment.api.domain.model

enum class TransactionStatus {
    SCHEDULED, PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED;

    fun canTransitionTo(target: TransactionStatus): Boolean = when (this) {
        SCHEDULED -> target in setOf(PENDING, CANCELLED)
        PENDING -> target in setOf(PROCESSING, CANCELLED)
        PROCESSING -> target in setOf(COMPLETED, FAILED)
        COMPLETED, FAILED, CANCELLED -> false
    }

    val isTerminal: Boolean
        get() = this in setOf(COMPLETED, FAILED, CANCELLED)
}
