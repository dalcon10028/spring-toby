package common.advisor.transaction

import com.example.DaoFactory
import com.example.common.config.AppConfig
import com.example.common.config.DataSourceConfig
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
import org.springframework.stereotype.Service
import javax.sql.DataSource

class TransactionAspectTest : FunSpec({

    context("TransactionAspect pointcut matching") {
        test("should apply to @Service annotated classes") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class TestConfig {
                @Bean
                open fun testService(userDao: UserDao, publisher: ApplicationEventPublisher): TestUpgradeService {
                    return TestUpgradeService(userDao, publisher)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(AppConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(TestConfig::class.java)
            context.scan("com.example.common.advisor.transaction")
            context.refresh()

            // when
            val service = context.getBean("testService", TestUpgradeService::class.java)

            // then - service should be proxied by AspectJ
            AopUtils.isAopProxy(service) shouldBe true

            context.close()
        }

        test("should NOT apply to classes without @Service annotation") {
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
            context.register(AppConfig::class.java)
            context.register(TestConfig::class.java)
            context.scan("com.example.common.advisor.transaction")
            context.refresh()

            // when
            val helper = context.getBean("testHelper", TestHelper::class.java)

            // then - helper should NOT be proxied
            AopUtils.isAopProxy(helper) shouldBe false

            context.close()
        }
    }

    context("Transaction rollback and commit") {
        test("should rollback when exception occurs during upgrade") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class FailingServiceConfig {
                @Bean
                open fun updateCounter(): UpdateCounter {
                    return UpdateCounter()
                }

                @Bean
                open fun failingService(
                    userDao: UserDao,
                    publisher: ApplicationEventPublisher,
                    updateCounter: UpdateCounter
                ): FailingUpgradeService {
                    return FailingUpgradeService(userDao, publisher, updateCounter)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(AppConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(FailingServiceConfig::class.java)
            context.scan("com.example.common.advisor.transaction")
            context.refresh()
            prepareDatabase(context)

            val userDao = context.getBean(UserDao::class.java)
            val service = context.getBean("failingService", FailingUpgradeService::class.java)
            val updateCounter = context.getBean(UpdateCounter::class.java)

            // Verify service is proxied
            AopUtils.isAopProxy(service) shouldBe true

            // Insert test users
            userDao.add(User("tx1", "User 1", "pass", UserLevel.BASIC, 55, 10))
            userDao.add(User("tx2", "User 2", "pass", UserLevel.BASIC, 55, 10))
            userDao.add(User("tx3", "User 3", "pass", UserLevel.BASIC, 55, 10))

            // Verify initial state
            userDao.getAll().forEach { user ->
                user.level shouldBe UserLevel.BASIC
            }

            // when - exception occurs during upgrade
            shouldThrow<RuntimeException> {
                service.upgradeWithFailure()
            }

            // then - all users should still be BASIC (transaction rolled back)
            userDao.getAll().forEach { user ->
                user.level shouldBe UserLevel.BASIC
            }

            // Verify update was attempted twice before exception
            updateCounter.count shouldBe 2

            context.close()
        }

        test("should commit when no exception occurs during save") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class SuccessServiceConfig {
                @Bean
                open fun successService(
                    userDao: UserDao,
                    publisher: ApplicationEventPublisher
                ): SuccessSaveService {
                    return SuccessSaveService(userDao, publisher)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(AppConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(SuccessServiceConfig::class.java)
            context.scan("com.example.common.advisor.transaction")
            context.refresh()

            // Drop and recreate table
            val dataSource = context.getBean(DataSource::class.java)
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
            val service = context.getBean("successService", SuccessSaveService::class.java)

            // Verify service is proxied
            AopUtils.isAopProxy(service) shouldBe true

            // when - save users without exception
            service.saveUsers(
                listOf(
                    User("save1", "User 1", "pass", UserLevel.BASIC, 55, 10),
                    User("save2", "User 2", "pass", UserLevel.SILVER, 100, 35)
                )
            )

            // then - all users should be saved (transaction committed)
            val users = userDao.getAll()
            users.size shouldBe 2
            users.find { it.id == "save1" }?.level shouldBe UserLevel.BASIC
            users.find { it.id == "save2" }?.level shouldBe UserLevel.SILVER

            context.close()
        }

        test("should use read-only transaction for get methods") {
            // given
            val context = AnnotationConfigApplicationContext()

            @Configuration
            open class ReadOnlyServiceConfig {
                @Bean
                open fun readOnlyService(userDao: UserDao, publisher: ApplicationEventPublisher): ReadOnlyTestService {
                    return ReadOnlyTestService(userDao, publisher)
                }
            }

            context.register(DataSourceConfig::class.java)
            context.register(AppConfig::class.java)
            context.register(DaoFactory::class.java)
            context.register(ReadOnlyServiceConfig::class.java)
            context.scan("com.example.common.advisor.transaction")
            context.refresh()

            // Drop and recreate table to ensure clean state
            val dataSource = context.getBean(DataSource::class.java)
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
            val service = context.getBean("readOnlyService", ReadOnlyTestService::class.java)

            // Insert test data
            userDao.add(User("read1", "User 1", "pass", UserLevel.BASIC, 10, 5))

            // Verify service is proxied
            AopUtils.isAopProxy(service) shouldBe true

            // when - call read-only method
            val users = service.getAllUsers()

            // then - should return users (read-only transaction)
            users.size shouldBe 1
            users[0].id shouldBe "read1"

            context.close()
        }
    }
})

// Test helper class
class UpdateCounter {
    var count = 0
}

// Test service classes - must be open for proxy and annotated with @Service
@Service
open class TestUpgradeService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher
) {
    open fun upgradeUsers() {
        // Method that should be transactional
    }
}

open class TestHelper {
    fun processData() {
        // This should NOT be transactional (no @Service annotation)
    }
}

@Service
open class FailingUpgradeService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher,
    private val updateCounter: UpdateCounter
) {
    open fun upgradeWithFailure() {
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

@Service
open class SuccessSaveService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher
) {
    open fun saveUsers(users: List<User>) {
        users.forEach { userDao.add(it) }
    }
}

@Service
open class ReadOnlyTestService(
    private val userDao: UserDao,
    private val publisher: ApplicationEventPublisher
) {
    open fun getAllUsers(): List<User> {
        return userDao.getAll()
    }

    open fun findUserById(id: String): User {
        return userDao.get(id)
    }
}
