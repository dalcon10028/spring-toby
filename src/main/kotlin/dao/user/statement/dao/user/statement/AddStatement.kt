package com.example.dao.user.statement.dao.user.statement

import com.example.User
import com.example.dao.StatementStrategy
import java.sql.Connection
import java.sql.PreparedStatement

class AddStatement(
    private val user: User
) : StatementStrategy {
    override fun makePreparedStatement(connection: Connection): PreparedStatement {
        val ps = connection.prepareStatement("INSERT INTO users(id, name, password) VALUES(?, ?, ?)")
        ps.setString(1, user.id)
        ps.setString(2, user.name)
        ps.setString(3, user.password)
        return ps
    }
}