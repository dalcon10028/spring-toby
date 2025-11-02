package study

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.FactoryBean

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