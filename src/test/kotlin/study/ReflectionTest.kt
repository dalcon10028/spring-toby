package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.framework.ProxyFactoryBean
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.NameMatchMethodPointcut
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

private interface Hello {
    fun sayHello(name: String): String
    fun sayGoodbye(name: String): String
}

private class HelloImpl : Hello {
    override fun sayHello(name: String): String = "Hello, $name"
    override fun sayGoodbye(name: String): String = "Goodbye, $name"
}

private class UppercaseAdvice : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        val result = invocation.proceed()
        return if (result is String) result.uppercase() else result
    }
}

class ReflectionTest : FunSpec({
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

            val helloClass = Class.forName("study.HelloImpl")
            val helloInstance = helloClass.getDeclaredConstructor().newInstance() as Hello

            val proxyInstance = HelloUppercase(helloInstance)
            val helloMessage = proxyInstance.sayHello("John")
            helloMessage shouldBe "HELLO, JOHN"

            val goodbyeMessage = proxyInstance.sayGoodbye("John")
            goodbyeMessage shouldBe "GOODBYE, JOHN"
        }

        test("Creating implementation via dynamic proxy") {
            class HelloInvocationHandler(
                private val target: Hello
            ) : InvocationHandler {
                override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                    val result = args?.let {
                        method.invoke(target, *it)
                    } ?: method.invoke(target)
                    return (result as String).uppercase()
                }
            }

            val helloClass = Class.forName("study.HelloImpl")
            val helloInstance = helloClass.getDeclaredConstructor().newInstance() as Hello
            val proxyInstance = Proxy.newProxyInstance(
                Hello::class.java.classLoader,
                arrayOf(Hello::class.java),
                HelloInvocationHandler(helloInstance)
            ) as Hello
            val helloMessage = proxyInstance.sayHello("John")
            helloMessage shouldBe "HELLO, JOHN"
            val goodbyeMessage = proxyInstance.sayGoodbye("John")
            goodbyeMessage shouldBe "GOODBYE, JOHN"
        }

        // 확장된 다이나믹 프록시 테스트
        test("Creating implementation via extended dynamic proxy") {
            class UppercaseHandler(
                private val target: Any
            ) : InvocationHandler {
                override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {
                    val result = args?.let {
                        method.invoke(target, *it)
                    } ?: method.invoke(target)
                    return if (result is String) result.uppercase() else result
                }
            }

            val helloClass = Class.forName("study.HelloImpl")
            val helloInstance = helloClass.getDeclaredConstructor().newInstance() as Hello
            val proxyInstance = Proxy.newProxyInstance(
                Hello::class.java.classLoader,
                arrayOf(Hello::class.java),
                UppercaseHandler(helloInstance)
            ) as Hello
            val helloMessage = proxyInstance.sayHello("John")
            helloMessage shouldBe "HELLO, JOHN"
            val goodbyeMessage = proxyInstance.sayGoodbye("John")
            goodbyeMessage shouldBe "GOODBYE, JOHN"
        }
    }

    test("Using Spring's ProxyFactoryBean to create a proxy with advice") {
        val proxyFactoryBean = ProxyFactoryBean().also {
            it.setTarget(HelloImpl())
            it.addAdvice(UppercaseAdvice())
        }

        val hello = proxyFactoryBean.getObject() as Hello
        hello.sayHello("World") shouldBe "HELLO, WORLD"
        hello.sayGoodbye("World") shouldBe "GOODBYE, WORLD"
    }

    test("Using ProxyFactoryBean with Pointcut and Advice") {
        val nameMatchMethodPointcut = object : NameMatchMethodPointcut() {
            init {
                setMappedNames("sayH*")
            }
        }

        val proxyFactoryBean = ProxyFactoryBean().also {
            it.setTarget(HelloImpl())
            it.addAdvisor(
                /**
                 * `ProxyFactoryBean` 에는 여러 개의 어드바이스와 포인트컷을 추가할 수 있다.
                 * Advisor 타입의 오브젝트에 담아서 조합을 만들어 등록한다.
                 */
                DefaultPointcutAdvisor(
                    nameMatchMethodPointcut,
                    UppercaseAdvice()
                )
            )
        }
        val hello = proxyFactoryBean.getObject() as Hello
        hello.sayHello("World") shouldBe "HELLO, WORLD"
        hello.sayGoodbye("World") shouldBe "Goodbye, World"
    }
})