package com.example.impl

import com.example.ConnectionMaker
import java.sql.Connection
import java.sql.DriverManager

class NConnectionMaker : ConnectionMaker {
    override fun makeNewConnection(): Connection =
        DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")
}