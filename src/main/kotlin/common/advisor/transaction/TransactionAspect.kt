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
    @Pointcut("""
        execution(* *..*Service.upgrade*(..)) ||
        execution(* *..*Service.save*(..)) ||
        execution(* *..*Service.update*(..)) ||
        execution(* *..*Service.delete*(..))
    """)
    private fun transactionPointcut() {}

    @Around("transactionPointcut()")
    fun aroundTransaction(joinPoint: ProceedingJoinPoint): Any? {
        val txStatus = transactionManager.getTransaction(
            DefaultTransactionDefinition()
        )
        return try {
            val result = joinPoint.proceed()
            transactionManager.commit(txStatus)
            result
        } catch (ex: Exception) {
            transactionManager.rollback(txStatus)
            throw ex
        }
    }
}