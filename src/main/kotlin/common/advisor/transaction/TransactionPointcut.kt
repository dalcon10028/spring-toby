package com.example.common.advisor.transaction

import org.springframework.aop.aspectj.AspectJExpressionPointcut

class TransactionPointcut : AspectJExpressionPointcut() {
    init {
        expression = """
            execution(* *..*Service.upgrade*(..)) ||
            execution(* *..*Service.save*(..)) ||
            execution(* *..*Service.update*(..)) ||
            execution(* *..*Service.delete*(..))
        """.trimIndent()
    }
}