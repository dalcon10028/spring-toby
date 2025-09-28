package com.example.dao

import java.sql.Connection
import java.sql.PreparedStatement

interface StatementStrategy {
    fun makePreparedStatement(connection: Connection): PreparedStatement
}