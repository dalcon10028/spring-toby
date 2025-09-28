package com.example.dao.user.statement

import com.example.dao.StatementStrategy
import java.sql.Connection
import java.sql.PreparedStatement

class DeleteAllStatement : StatementStrategy {
    override fun makePreparedStatement(connection: Connection): PreparedStatement {
        return connection.prepareStatement("DELETE FROM users")
    }
}