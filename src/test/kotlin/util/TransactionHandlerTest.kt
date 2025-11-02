package util

import com.example.util.TransactionHandler
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import java.lang.reflect.Proxy

// 테스트용 인터페이스
interface TestService {
    fun upgradeLevels(): String
    fun add(value: Int): Int
    fun getNonTransactionalData(): String
}

// 실제 구현체
class TestServiceImpl : TestService {
    override fun upgradeLevels(): String = "upgraded"
    override fun add(value: Int): Int = value + 10
    override fun getNonTransactionalData(): String = "data"
}

class TransactionHandlerTest : FunSpec({
    context("TransactionHandler with Dynamic Proxy") {
        test("should execute method with transaction when pattern matches") {
            // Given
            val txManager = mockk<PlatformTransactionManager>()
            val txStatus = mockk<TransactionStatus>()
            val target = TestServiceImpl()

            every { txManager.getTransaction(null) } returns txStatus
            every { txManager.commit(txStatus) } just Runs

            // TransactionHandler 생성 (upgrade로 시작하는 메소드에만 트랜잭션 적용)
            val handler = TransactionHandler(txManager, "upgrade.*")
            // reflection으로 target 설정
            val targetField = TransactionHandler::class.java.getDeclaredField("target")
            targetField.isAccessible = true
            targetField.set(handler, target)

            // Dynamic Proxy 생성
            val proxy = Proxy.newProxyInstance(
                TestService::class.java.classLoader,
                arrayOf(TestService::class.java),
                handler
            ) as TestService

            // When
            val result = proxy.upgradeLevels()

            // Then
            result shouldBe "upgraded"
            verify(exactly = 1) { txManager.getTransaction(null) }
            verify(exactly = 1) { txManager.commit(txStatus) }
            verify(exactly = 0) { txManager.rollback(any()) }
        }

        test("should not apply transaction when pattern does not match") {
            // Given
            val txManager = mockk<PlatformTransactionManager>()
            val target = TestServiceImpl()

            val handler = TransactionHandler(txManager, "upgrade.*")
            val targetField = TransactionHandler::class.java.getDeclaredField("target")
            targetField.isAccessible = true
            targetField.set(handler, target)

            val proxy = Proxy.newProxyInstance(
                TestService::class.java.classLoader,
                arrayOf(TestService::class.java),
                handler
            ) as TestService

            // When
            val result = proxy.getNonTransactionalData()

            // Then
            result shouldBe "data"
            verify(exactly = 0) { txManager.getTransaction(any()) }
            verify(exactly = 0) { txManager.commit(any()) }
        }

        test("should rollback transaction when exception occurs") {
            // Given
            val txManager = mockk<PlatformTransactionManager>()
            val txStatus = mockk<TransactionStatus>()
            val target = mockk<TestService>()

            every { txManager.getTransaction(null) } returns txStatus
            every { txManager.rollback(txStatus) } just Runs
            every { target.upgradeLevels() } throws RuntimeException("DB Error")

            val handler = TransactionHandler(txManager, "upgrade.*")
            val targetField = TransactionHandler::class.java.getDeclaredField("target")
            targetField.isAccessible = true
            targetField.set(handler, target)

            val proxy = Proxy.newProxyInstance(
                TestService::class.java.classLoader,
                arrayOf(TestService::class.java),
                handler
            ) as TestService

            // When & Then
            shouldThrow<RuntimeException> {
                proxy.upgradeLevels()
            }

            verify(exactly = 1) { txManager.getTransaction(null) }
            verify(exactly = 0) { txManager.commit(any()) }
            verify(exactly = 1) { txManager.rollback(txStatus) }
        }

        test("should handle methods with arguments") {
            // Given
            val txManager = mockk<PlatformTransactionManager>()
            val txStatus = mockk<TransactionStatus>()
            val target = TestServiceImpl()

            every { txManager.getTransaction(null) } returns txStatus
            every { txManager.commit(txStatus) } just Runs

            val handler = TransactionHandler(txManager, "add")
            val targetField = TransactionHandler::class.java.getDeclaredField("target")
            targetField.isAccessible = true
            targetField.set(handler, target)

            val proxy = Proxy.newProxyInstance(
                TestService::class.java.classLoader,
                arrayOf(TestService::class.java),
                handler
            ) as TestService

            // When
            val result = proxy.add(5)

            // Then
            result shouldBe 15
            verify(exactly = 1) { txManager.getTransaction(null) }
            verify(exactly = 1) { txManager.commit(txStatus) }
        }
    }
})
