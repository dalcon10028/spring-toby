package service

import com.example.dao.user.UserDao
import com.example.model.User
import com.example.model.UserLevel
import com.example.service.user.UserService
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.context.ApplicationEventPublisher

class UserServiceTest : FunSpec({
    val userDao = mockk<UserDao>()
    val publisher = mockk<ApplicationEventPublisher>(relaxed = true)
    val userService = UserService(userDao, publisher)

    beforeEach { clearAllMocks() }

    context("upgradeLevels") {
        test("should upgrade BASIC user to SILVER when login >= 50") {
            // given
            val basicUser = User(
                id = "user1",
                name = "Test User",
                password = "password",
                level = UserLevel.BASIC,
                login = 55,
                recommend = 10
            )
            every { userDao.getAll() } returns listOf(basicUser)
            every { userDao.update(any()) } returns Unit

            // when
            userService.upgradeLevels()

            // then
            verify {
                userDao.update(match { user ->
                    user.id == "user1" && user.level == UserLevel.SILVER
                })
            }
        }

        test("should not upgrade BASIC user when login < 50") {
            // given
            val basicUser = User(
                id = "user1",
                name = "Test User",
                password = "password",
                level = UserLevel.BASIC,
                login = 40,
                recommend = 10
            )
            every { userDao.getAll() } returns listOf(basicUser)

            // when
            userService.upgradeLevels()

            // then
            verify(exactly = 0) { userDao.update(any()) }
        }

        test("should upgrade SILVER user to GOLD when recommend >= 30") {
            // given
            val silverUser = User(
                id = "user2",
                name = "Silver User",
                password = "password",
                level = UserLevel.SILVER,
                login = 100,
                recommend = 35
            )
            every { userDao.getAll() } returns listOf(silverUser)
            every { userDao.update(any()) } returns Unit

            // when
            userService.upgradeLevels()

            // then
            verify {
                userDao.update(match { user ->
                    user.id == "user2" && user.level == UserLevel.GOLD
                })
            }
        }

        test("should not upgrade GOLD user") {
            // given
            val goldUser = User(
                id = "user3",
                name = "Gold User",
                password = "password",
                level = UserLevel.GOLD,
                login = 100,
                recommend = 100
            )
            every { userDao.getAll() } returns listOf(goldUser)

            // when
            userService.upgradeLevels()

            // then
            verify(exactly = 0) { userDao.update(any()) }
        }
    }

    context("add") {
        test("should add user through userDao") {
            // given
            val newUser = User(
                id = "newUser",
                name = "New User",
                password = "password",
                level = UserLevel.BASIC,
                login = 0,
                recommend = 0
            )
            every { userDao.add(newUser) } returns Unit

            // when
            userService.add(newUser)

            // then
            verify(exactly = 1) { userDao.add(newUser) }
        }
    }
})
