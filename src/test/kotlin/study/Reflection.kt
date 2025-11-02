package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

interface Hello {
    fun sayHello(name: String): String
    fun sayGoodbye(name: String): String
}

class Reflection : FunSpec({
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

    context("proxy class") {
        class HelloImpl : Hello {
            override fun sayHello(name: String): String = "Hello, $name"
            override fun sayGoodbye(name: String): String = "Goodbye, $name"
        }

        test("Creating implementation via anonymous class") {
            val helloInstance = HelloImpl()
            val helloMessage = helloInstance.sayHello("John")
            helloMessage shouldBe "Hello, John"

            val goodbyeMessage = helloInstance.sayGoodbye("John")
            goodbyeMessage shouldBe "Goodbye, John"
        }

        test("Creating implementation via reflection") {
            class HelloUppercase(
                val target: Hello
            ) : Hello {
                override fun sayHello(name: String): String = target.sayHello(name).uppercase()
                override fun sayGoodbye(name: String): String = target.sayGoodbye(name).uppercase()
            }

            val helloClass = Class.forName($$"study.Reflection$1$2$HelloImpl")
            val helloInstance = helloClass.getDeclaredConstructor().newInstance() as Hello

            val proxyInstance = HelloUppercase(helloInstance)
            val helloMessage = proxyInstance.sayHello("John")
            helloMessage shouldBe "HELLO, JOHN"

            val goodbyeMessage = proxyInstance.sayGoodbye("John")
            goodbyeMessage shouldBe "GOODBYE, JOHN"
        }

    }
})