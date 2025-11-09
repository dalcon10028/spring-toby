package com.example.common.config

import org.h2.Driver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
open class DataSourceConfig {

    @Bean
    open fun dataSource(): DataSource {
        val dataSource = SimpleDriverDataSource()
        dataSource.setDriverClass(Driver::class.java)
        dataSource.url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
        dataSource.username = "sa"
        dataSource.password = ""
        return dataSource
    }

    @Bean
    open fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }
}