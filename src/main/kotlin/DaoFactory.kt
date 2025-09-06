package com.example

import com.example.dao.AccountDao
import com.example.dao.MessageDao
import com.example.dao.UserDao
import com.example.impl.DConnectionMaker
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class DaoFactory {

    @Bean
    open fun userDao(): UserDao = UserDao(connectionMaker())

    @Bean
    open fun accountDao(): AccountDao = AccountDao(connectionMaker())

    @Bean
    open fun messageDao(): MessageDao = MessageDao(connectionMaker())

    @Bean
    open fun connectionMaker(): ConnectionMaker = DConnectionMaker()
}