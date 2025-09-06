package com.example

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SimpleConnectionMaker {

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun makeNewConnection(): Connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")
}