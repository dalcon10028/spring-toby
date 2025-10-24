package com.example.service

import com.example.dao.user.UserDao
import com.example.model.User
import com.example.model.UserLevel.*
import com.example.model.UserLevelUpgradeEvent
import com.example.util.TransactionUtils.transaction
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager

@Service
class UserService(
    private val userDao: UserDao,
    private val tx: PlatformTransactionManager,
    private val publisher: ApplicationEventPublisher,
) {
    fun add(user: User) = userDao.add(user)

    fun upgradeLevels() = tx.transaction {
        userDao.getAll()
            .filterNot { user -> user.level == GOLD } // Exclude GOLD level users
            .filter { user ->
                // Check if the user meets the criteria for level upgrade
                when (user.level) {
                    BASIC -> user.login >= 50
                    SILVER -> user.recommend >= 30
                    else -> false
                }
            }
            // Upgrade the user's level and update in the database
            .map { user -> user.copy(level = user.level.nextLevel()) }
            .onEach { user -> userDao.update(user) }
            // Publish an event for each upgraded user
            .forEach { user -> publisher.publishEvent(UserLevelUpgradeEvent(user)) }
    }
}