package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Reflection: FunSpec({
    context("invoke method by reflection") {
        test("Method invocation using reflection") {
            val name = "Kotlin"
            val lengthMethod = String::class.java.getMethod("length")
            val length = lengthMethod.invoke(name) as Int
            length shouldBe 6

            val upperCaseMethod = String::class.java.getMethod("toUpperCase")
            val upperCaseResult = upperCaseMethod.invoke(name) as String
            upperCaseResult shouldBe "KOTLIN"
        }
    }
})