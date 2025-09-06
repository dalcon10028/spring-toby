package com.example

import com.example.dao.AccountDao
import com.example.dao.MessageDao
import com.example.dao.UserDao
import com.example.impl.DConnectionMaker

class DaoFactory {
    fun userDao(): UserDao = UserDao(connectionMaker())

    fun accountDao(): AccountDao = AccountDao(connectionMaker())

    fun messageDao(): MessageDao = MessageDao(connectionMaker())

    fun connectionMaker(): ConnectionMaker = DConnectionMaker()
}