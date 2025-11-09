package com.example.common.advisor.transaction

import org.springframework.aop.support.DefaultPointcutAdvisor

class TransactionAdvisor(
    transactionAdvice: TransactionAdvice,
    transactionPointcut: TransactionPointcut,
) : DefaultPointcutAdvisor(transactionPointcut, transactionAdvice)