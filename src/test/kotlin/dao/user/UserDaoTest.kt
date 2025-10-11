package dao.user

import com.example.User
import com.example.dao.user.UserDao
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException

class UserDaoTest : FunSpec({

    lateinit var userDao: UserDao

    beforeEach {
        userDao = mockk(relaxed = true)
    }

    test("add and get") {
        // given
        val user = User("testId", "testName", "testPassword")
        every { userDao.get("testId") } returns user

        // when
        userDao.add(user)
        val foundUser = userDao.get("testId")

        // then
        verify { userDao.add(user) }
        foundUser shouldBe user
    }

    test("add 시 중복 키 예외가 발생해야 한다") {
        // given
        val user = User("testId", "testName", "testPassword")
        every { userDao.add(user) } throws DuplicateKeyException("Duplicate Key")

        // when & then
        shouldThrow<DuplicateKeyException> {
            userDao.add(user)
        }

        verify(exactly = 1) { userDao.add(user) }
    }

    test("getCount") {
        // given
        every { userDao.getCount() } returns 2

        // when
        val count = userDao.getCount()

        // then
        count shouldBe 2
        verify { userDao.getCount() }
    }

    test("getAll") {
        // given
        val users = listOf(
            User("id1", "name1", "pw1"),
            User("id2", "name2", "pw2")
        )
        every { userDao.getAll() } returns users

        // when
        val allUsers = userDao.getAll()

        // then
        allUsers shouldBe users
        verify { userDao.getAll() }
    }

    test("deleteAll") {
        // given
        every { userDao.deleteAll() } returns Unit

        // when
        userDao.deleteAll()

        // then
        verify { userDao.deleteAll() }
    }

    test("존재하지 않는 사용자 조회시 예외 발생") {
        // given
        every { userDao.get("nonExistentId") } throws EmptyResultDataAccessException(1)

        // when & then
        shouldThrow<EmptyResultDataAccessException> {
            userDao.get("nonExistentId")
        }

        verify { userDao.get("nonExistentId") }
    }
})