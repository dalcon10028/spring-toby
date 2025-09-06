package com.example.impl

import com.example.UserDao
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class NUserDao : UserDao() {

    @Throws(SQLException::class, ClassNotFoundException::class)
    override fun getConnection(): Connection {
        Class.forName("org.h2.Driver")
        return DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")
    }
}