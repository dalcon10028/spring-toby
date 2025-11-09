package common.config

import com.example.DaoFactory
import com.example.common.config.DataSourceConfig
import com.example.common.config.TransactionConfig
import com.example.dao.user.UserDao
import com.example.model.User
import com.example.model.UserLevel
import com.example.prepareDatabase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.springframework.aop.support.AopUtils
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class TransactionConfigTest : FunSpec({

    context("TransactionAdvisor pointcut") {
        test("should apply to *Service classes with upgrade* methods") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class TestServiceConfig {
                @Bean
                open fun simpleTestService(userDao: UserDao, publisher: ApplicationEventPublisher): SimpleTestService {
                    return SimpleTestService(userDao, publisher)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(TransactionConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(TestServiceConfig::class.java)
            context.refresh()

            // when
            val service = context.getBean("simpleTestService", SimpleTestService::class.java)

            // Debug: print all advisors
            val advisors = context.getBeanNamesForType(org.springframework.aop.Advisor::class.java)
            println("Advisors in context: ${advisors.toList()}")

            // then - service should be proxied because class name ends with 'Service'
            println("Service class: ${service.javaClass.name}")
            println("Is AOP proxy: ${AopUtils.isAopProxy(service)}")
            println("Is CGLIB proxy: ${AopUtils.isCglibProxy(service)}")

            AopUtils.isAopProxy(service) shouldBe true
            AopUtils.isCglibProxy(service) shouldBe true

            context.close()
        }

        test("should NOT apply to classes that don't end with Service") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class TestConfig {
                @Bean
                open fun testHelper(): TestHelper {
                    return TestHelper()
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(TransactionConfig::class.java)
            context.register(TestConfig::class.java)
            context.refresh()

            // when
            val helper = context.getBean("testHelper", TestHelper::class.java)

            // then - helper should NOT be proxied (class name doesn't end with 'Service')
            AopUtils.isAopProxy(helper) shouldBe false

            context.close()
        }
    }

    context("Transaction rollback") {
        test("should rollback when exception occurs in upgradeLevels") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class FailingServiceConfig {

                @Bean
                open fun updateCounter(): UpdateCounter {
                    return UpdateCounter()
                }

                @Bean
                open fun failingService(userDao: UserDao, publisher: ApplicationEventPublisher, updateCounter: UpdateCounter): FailingUpgradeService {
                    return FailingUpgradeService(userDao, publisher, updateCounter)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(TransactionConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(FailingServiceConfig::class.java)
            context.refresh()
            prepareDatabase(context)

            val userDao = context.getBean(UserDao::class.java)
            val service = context.getBean("failingService", FailingUpgradeService::class.java)
            val updateCounter = context.getBean(UpdateCounter::class.java)

            // Verify service is proxied
            AopUtils.isAopProxy(service) shouldBe true

            // Insert test users
            userDao.add(User("user1", "User 1", "pass", UserLevel.BASIC, 55, 10))
            userDao.add(User("user2", "User 2", "pass", UserLevel.BASIC, 55, 10))
            userDao.add(User("user3", "User 3", "pass", UserLevel.BASIC, 55, 10))

            // Verify initial state
            userDao.getAll().forEach { user ->
                user.level shouldBe UserLevel.BASIC
            }

            // when - exception occurs during upgrade
            shouldThrow<RuntimeException> {
                service.upgradeLevelsWithFailure()
            }

            // then - all users should still be BASIC (transaction rolled back)
            userDao.getAll().forEach { user ->
                user.level shouldBe UserLevel.BASIC
            }

            // Verify update was attempted twice before exception
            updateCounter.count shouldBe 2

            context.close()
        }

        test("should commit when no exception occurs in upgradeLevels") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class SuccessServiceConfig {
                @Bean
                open fun successService(userDao: UserDao, publisher: ApplicationEventPublisher): SuccessUpgradeService {
                    return SuccessUpgradeService(userDao, publisher)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(TransactionConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(SuccessServiceConfig::class.java)
            context.refresh()

            // Drop and recreate table to avoid duplicate key issues
            val dataSource = context.getBean(javax.sql.DataSource::class.java)
            dataSource.connection.use { conn ->
                conn.createStatement().execute("DROP TABLE IF EXISTS users")
                conn.createStatement().execute(
                    """
                    CREATE TABLE users (
                        id VARCHAR(50) PRIMARY KEY,
                        name VARCHAR(100),
                        password VARCHAR(100),
                        level INT,
                        login INT,
                        recommend INT
                    )
                    """.trimIndent()
                )
            }

            val userDao = context.getBean(UserDao::class.java)
            val service = context.getBean("successService", SuccessUpgradeService::class.java)

            // Verify service is proxied
            AopUtils.isAopProxy(service) shouldBe true

            // Insert test users with different IDs
            userDao.add(User("commit1", "User 1", "pass", UserLevel.BASIC, 55, 10))
            userDao.add(User("commit2", "User 2", "pass", UserLevel.BASIC, 55, 10))

            // when - upgrade without exception
            service.upgradeAllUsers()

            // then - all users should be upgraded to SILVER (transaction committed)
            userDao.getAll().forEach { user ->
                user.level shouldBe UserLevel.SILVER
            }

            context.close()
        }
    }
})

// Helper class to avoid Kotlin closure capture issues
class UpdateCounter {
    var count = 0
}

// Test classes - must be open for CGLIB proxy
open class SimpleTestService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher
) {
    open fun upgradeUsers() {
        // Method that should be transactional (upgrade* pattern)
    }
}

open class TestHelper {
    fun updateData() {
        // This should NOT be transactional (class doesn't end with 'Service')
    }
}

open class FailingUpgradeService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher,
    private val updateCounter: UpdateCounter
) {
    open fun upgradeLevelsWithFailure() {
        userDao.getAll()
            .filterNot { user -> user.level == UserLevel.GOLD }
            .filter { user ->
                when (user.level) {
                    UserLevel.BASIC -> user.login >= 50
                    UserLevel.SILVER -> user.recommend >= 30
                    else -> false
                }
            }
            .map { user -> user.copy(level = user.level.nextLevel()) }
            .forEach { user ->
                userDao.update(user)
                updateCounter.count++
                if (updateCounter.count == 2) {
                    throw RuntimeException("Intentional failure for rollback test")
                }
            }
    }
}

open class SuccessUpgradeService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher
) {
    open fun upgradeAllUsers() {
        userDao.getAll()
            .filter { it.level == UserLevel.BASIC && it.login >= 50 }
            .map { it.copy(level = UserLevel.SILVER) }
            .forEach { userDao.update(it) }
    }
}
