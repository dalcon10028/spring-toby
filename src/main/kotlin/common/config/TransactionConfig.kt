package com.example.common.config

import com.example.common.advisor.transaction.TransactionAdvice
import com.example.common.advisor.transaction.TransactionAdvisor
import com.example.common.advisor.transaction.TransactionPointcut
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
open class TransactionConfig {

    @Bean
    open fun transactionManager(dataSource: DataSource): PlatformTransactionManager {
        return DataSourceTransactionManager(dataSource)
    }

    @Bean
    open fun transactionAdvice(transactionManager: PlatformTransactionManager): TransactionAdvice {
        return TransactionAdvice(transactionManager)
    }

    @Bean
    open fun transactionPointcut(): TransactionPointcut {
        return TransactionPointcut()
    }

    @Bean
    open fun transactionAdvisor(
        transactionAdvice: TransactionAdvice,
        transactionPointcut: TransactionPointcut
    ): TransactionAdvisor {
        return TransactionAdvisor(transactionAdvice, transactionPointcut)
    }
}

