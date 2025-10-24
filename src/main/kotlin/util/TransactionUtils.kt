package com.example.util

import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionSynchronizationManager
import javax.sql.DataSource

object TransactionUtils {

    inline fun <T> DataSource.transaction(block: () -> T): T {
        val connection = DataSourceUtils.getConnection(this)
        val previousAutoCommit = connection.autoCommit

        return try {
            connection.autoCommit = false
            TransactionSynchronizationManager.bindResource(this, connection)

            val result = block()

            connection.commit()
            result
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            TransactionSynchronizationManager.unbindResource(this)
            connection.autoCommit = previousAutoCommit
            DataSourceUtils.releaseConnection(connection, this)
        }
    }
}
