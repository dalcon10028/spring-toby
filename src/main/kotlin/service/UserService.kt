package com.example.service

import com.example.dao.user.UserDao
import com.example.model.UserLevel.*
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userDao: UserDao
) {
    fun upgradeLevels() {
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
            .forEach { user -> userDao.update(user) }
    }
}