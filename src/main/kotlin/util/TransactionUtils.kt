package com.example.util

import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition

object TransactionUtils {

    inline fun <T> PlatformTransactionManager.transaction(
        isolationLevel: Int = TransactionDefinition.ISOLATION_DEFAULT,
        propagationBehavior: Int = TransactionDefinition.PROPAGATION_REQUIRED,
        block: () -> T
    ): T {
        val transactionDefinition = DefaultTransactionDefinition().apply {
            this.isolationLevel = isolationLevel
            this.propagationBehavior = propagationBehavior
        }
        val transactionStatus = getTransaction(transactionDefinition)

        return try {
            val result = block()
            commit(transactionStatus)
            result
        } catch (e: Exception) {
            rollback(transactionStatus)
            throw e
        }
    }
}
