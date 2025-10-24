package com.example.model

enum class UserLevel(val value: Int) {
    BASIC(1),
    SILVER(2),
    GOLD(3);

    fun nextLevel(): UserLevel? {
        return when (this) {
            BASIC -> SILVER
            SILVER -> GOLD
            GOLD -> null
        }
    }

    companion object {
        fun from(value: Int): UserLevel = entries.find { it.value == value } ?:
            throw IllegalArgumentException("Unknown UserLevel value: $value")
    }
}

data class User(
    val id: String,
    val name: String,
    val password: String,
    val level: UserLevel = UserLevel.BASIC,
    val login: Int = 0,
    val recommend: Int = 0,
)