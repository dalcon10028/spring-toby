package com.example.model

import org.springframework.context.ApplicationEvent

enum class UserLevel(val value: Int) {
    BASIC(1),
    SILVER(2),
    GOLD(3);

    fun nextLevel(): UserLevel {
        return when (this) {
            BASIC -> SILVER
            SILVER -> GOLD
            GOLD -> GOLD
        }
    }

    companion object {
        fun from(value: Int): UserLevel =
            entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown UserLevel value: $value")
    }
}

data class UserLevelUpgradeEvent(
    val user: User
) : ApplicationEvent(user)

data class User(
    val id: String,
    val name: String,
    val password: String,
    val level: UserLevel = UserLevel.BASIC,
    val login: Int = 0,
    val recommend: Int = 0,
    val email: String = ""
)