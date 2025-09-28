package com.example.dao

import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource

class JdbcContext(
    val dataSource: DataSource
) {
    fun workWithStatement(statementStrategy: StatementStrategy) {
        var connection: Connection? = null
        var ps: PreparedStatement? = null

        try {
            connection = dataSource.connection
            ps = statementStrategy.makePreparedStatement(connection)
            ps.executeUpdate()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            try {
                ps?.close()
            } catch (e: Exception) {
            }
            try {
                connection?.close()
            } catch (e: Exception) {
            }
        }
    }

    fun executeSql(sql: String) {
        workWithStatement(object : StatementStrategy {
            override fun makePreparedStatement(connection: Connection): PreparedStatement {
                return connection.prepareStatement(sql)
            }
        })
    }
}