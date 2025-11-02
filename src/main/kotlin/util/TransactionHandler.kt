package com.example.util

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.transaction.PlatformTransactionManager
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class TransactionHandler(
    private val transactionManager: PlatformTransactionManager,
    private val pattern: String,
) : InvocationHandler {
    private val logger = KotlinLogging.logger {}
    val target: Any? = null

    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        val methodName = method.name
        val isTransactionMethod = methodName.matches(Regex(pattern))

        return if (isTransactionMethod) {
            logger.info { "Starting transaction for method: $methodName" }
            val status = transactionManager.getTransaction(null)
            return try {
                val result = args?.let { method.invoke(target, *it) } ?: method.invoke(target)
                transactionManager.commit(status)
                logger.info { "Transaction committed for method: $methodName" }
                result
            } catch (e: Exception) {
                transactionManager.rollback(status)
                logger.error(e) { "Transaction rolled back for method: $methodName due to exception." }
                throw e
            }
        } else {
            args?.let { method.invoke(target, *it) } ?: method.invoke(target)
        }
    }
}