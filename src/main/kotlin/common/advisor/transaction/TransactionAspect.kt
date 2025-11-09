package com.example.common.advisor.transaction

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition


@Aspect
@Component
class TransactionAspect(
    private val transactionManager: PlatformTransactionManager
) {
    @Pointcut("within(@org.springframework.stereotype.Service *)")
    private fun transactionPointcut() {
    }

    @Around("transactionPointcut()")
    fun aroundTransaction(proceedingJoinPoint: ProceedingJoinPoint): Any? {
        val methodName = proceedingJoinPoint.signature.name

        // if the method is a read-only operation, set the transaction as read-only
        val attributes = DefaultTransactionDefinition().apply {
            propagationBehavior = DefaultTransactionDefinition.PROPAGATION_REQUIRED
            isReadOnly = methodName.startsWith("get") || methodName.startsWith("find")
        }

        val status = transactionManager.getTransaction(attributes)
        return try {
            val result = proceedingJoinPoint.proceed()
            transactionManager.commit(status)
            result
        } catch (ex: Throwable) {
            transactionManager.rollback(status)
            throw ex
        }
    }
}