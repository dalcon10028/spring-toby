package com.example.dao.user

import com.example.User

interface UserDao {
    fun add(user: User)
    fun get(id: String): User
    fun deleteAll()
    fun getCount(): Int
    fun getAll(): List<User>
}