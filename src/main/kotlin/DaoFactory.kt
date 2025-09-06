package com.example

import com.example.impl.DConnectionMaker

class DaoFactory {
    fun userDao(): UserDao = UserDao(DConnectionMaker())
}