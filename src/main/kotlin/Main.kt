package com.example

/**
 * DB_CLOSE_DELAY=-1 : JVM이 살아있는 동안 메모리 DB 내용을 유지합니다.
 */
fun prepareDatabase() {

    Class.forName("org.h2.Driver")
    val connection = java.sql.DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "")
    val statement = connection.createStatement()
    statement.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
            id VARCHAR(50) PRIMARY KEY,
            name VARCHAR(100),
            password VARCHAR(100)
        )
        """.trimIndent()
    )
    statement.close()
    connection.close()
}

fun main() {
    prepareDatabase()
    val dao = DaoFactory().userDao()

    val user = User("1", "John Doe", "password123")
    dao.add(user)

    println("User added: $user")

    val user2 = dao.get(user.id)
    println("User retrieved: $user2")
}