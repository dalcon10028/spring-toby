package com.example.common.advisor.transaction

import org.springframework.aop.ClassFilter
import org.springframework.aop.support.NameMatchMethodPointcut

class TransactionPointcut : NameMatchMethodPointcut() {
    init {
        setMappedNames("save*", "update*", "delete*", "upgrade*")
        classFilter = ClassFilter { clazz ->
            clazz.simpleName.endsWith("Service")
        }
    }
}