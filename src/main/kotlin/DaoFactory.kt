package com.example

import com.example.dao.AccountDao
import com.example.dao.MessageDao
import com.example.dao.UserDao
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
open class DaoFactory(
    private val dataSource: DataSource,
) {

    @Bean
    open fun userDao(): UserDao = UserDao(dataSource)

    @Bean
    open fun accountDao(): AccountDao = AccountDao(dataSource)

    @Bean
    open fun messageDao(): MessageDao = MessageDao(dataSource)
}