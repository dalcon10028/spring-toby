package com.example

import java.sql.Connection
import java.sql.SQLException

interface ConnectionMaker {

    @Throws(ClassNotFoundException::class, SQLException::class)
    fun makeNewConnection(): Connection
}