package util

import com.example.util.TransactionUtils.transaction
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.Connection
import javax.sql.DataSource

class TransactionUtilsTest : FunSpec({

    val dataSource: DataSource = mockk()
    val connection: Connection = mockk(relaxed = true)

    beforeEach {
        clearAllMocks()

        every { dataSource.connection } returns connection
        mockkStatic(DataSourceUtils::class)
        every { DataSourceUtils.getConnection(dataSource) } returns connection
        every { DataSourceUtils.releaseConnection(connection, dataSource) } returns Unit

        mockkStatic(TransactionSynchronizationManager::class)
        every { TransactionSynchronizationManager.bindResource(any(), any()) } returns Unit
        every { TransactionSynchronizationManager.unbindResource(any()) } returns connection
    }

    test("should commit transaction when block executes successfully") {
        // given
        every { connection.autoCommit } returns true
        every { connection.autoCommit = false } just Runs
        every { connection.commit() } just Runs

        // when
        val result = dataSource.transaction {
            "success"
        }

        // then
        result shouldBe "success"
        verify {
            connection.autoCommit = false
            connection.commit()
            connection.autoCommit = true
        }
        verify(exactly = 0) { connection.rollback() }
    }

    test("should rollback transaction when exception occurs") {
        // given
        every { connection.autoCommit } returns true
        every { connection.autoCommit = false } just Runs
        every { connection.rollback() } just Runs

        // when & then
        shouldThrow<RuntimeException> {
            dataSource.transaction {
                throw RuntimeException("Test exception")
            }
        }

        verify {
            connection.autoCommit = false
            connection.rollback()
            connection.autoCommit = true
        }
        verify(exactly = 0) { connection.commit() }
    }

    test("should bind and unbind transaction resource") {
        // given
        every { connection.autoCommit } returns true
        every { connection.autoCommit = any() } just Runs
        every { connection.commit() } just Runs

        // when
        dataSource.transaction {
            "test"
        }

        // then
        verify {
            TransactionSynchronizationManager.bindResource(dataSource, connection)
            TransactionSynchronizationManager.unbindResource(dataSource)
        }
    }

    test("should restore previous autoCommit setting") {
        // given
        every { connection.autoCommit } returns false
        every { connection.autoCommit = any() } just Runs
        every { connection.commit() } just Runs

        // when
        dataSource.transaction {
            "test"
        }

        // then
        verifySequence {
            connection.autoCommit // get previous value (false)
            connection.autoCommit = false
            connection.commit()
            connection.autoCommit = false // restore to previous
        }
    }

    test("should release connection in finally block") {
        // given
        every { connection.autoCommit } returns true
        every { connection.autoCommit = any() } just Runs
        every { connection.rollback() } just Runs

        // when
        shouldThrow<RuntimeException> {
            dataSource.transaction {
                throw RuntimeException("Error")
            }
        }

        // then
        verify { DataSourceUtils.releaseConnection(connection, dataSource) }
    }

    test("should return result from transaction block") {
        // given
        every { connection.autoCommit } returns true
        every { connection.autoCommit = any() } just Runs
        every { connection.commit() } just Runs

        // when
        val result = dataSource.transaction {
            42
        }

        // then
        result shouldBe 42
    }
})
