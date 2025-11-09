package com.example.common.advisor.transaction

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition

class TransactionAdvice(
    private val transactionManager: PlatformTransactionManager
) : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        val txStatus = transactionManager.getTransaction(
            DefaultTransactionDefinition()
        )
        return try {
            val result = invocation.proceed()
            transactionManager.commit(txStatus)
            result
        } catch (ex: Exception) {
            transactionManager.rollback(txStatus)
            throw ex
        }
    }
}