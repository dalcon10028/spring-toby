package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.FactoryBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class FactoryBeanTest : FunSpec({

    context("FactoryBean Test") {

        test("should create bean using FactoryBean") {
            class MessageFactoryBean : FactoryBean<Message> {
                val messageText = "Hello from FactoryBean"

                override fun getObject(): Message = Message.create(messageText)

                override fun getObjectType(): Class<*> = Message::class.java

                override fun isSingleton(): Boolean = false
            }

            // FactoryBean 인스턴스 생성
            val factoryBean = MessageFactoryBean()
            // FactoryBean을 통해 실제 객체 생성
            val message = factoryBean.getObject()

            // 결과 검증
            message shouldBe Message.create("Hello from FactoryBean")
            val isSingleton = factoryBean.isSingleton()
            isSingleton shouldBe false
        }
    }

    // 팩토리빈을 스프링에 등록하고 가져오는 기능 테스트
    context("FactoryBean Registration Test") {
        val context = AnnotationConfigApplicationContext(AppConfig::class.java)
        val message1 = context.getBean("messageFactoryBean") as Message
        val message2 = context.getBean("messageFactoryBean") as Message

        test("should retrieve bean from Spring context using FactoryBean") {
            message1 shouldBe Message.create("Hello from Registered FactoryBean")
            message1 shouldBe message2 // Since isSingleton() returns true
        }
    }
})

class Message private constructor(
    private val text: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false
        return text == other.text
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    companion object {
        fun create(text: String): Message {
            return Message(text)
        }
    }
}

@Configuration
open class AppConfig {
    @Bean
    open fun messageFactoryBean(): FactoryBean<Message> {
        return object : FactoryBean<Message> {
            val messageText = "Hello from Registered FactoryBean"

            override fun getObject(): Message = Message.create(messageText)

            override fun getObjectType(): Class<*> = Message::class.java

            override fun isSingleton(): Boolean = true
        }
    }
}