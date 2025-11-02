package com.example

import com.example.config.DataSourceConfig
import com.example.dao.user.impl.UserDaoJdbc
import com.example.model.User
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

/**
 * DB_CLOSE_DELAY=-1 : JVM이 살아있는 동안 메모리 DB 내용을 유지합니다.
 */
fun prepareDatabase(context: ApplicationContext) {
    val connection = context.getBean(DataSourceConfig::class.java).dataSource().connection
    val statement = connection.createStatement()
    statement.execute(
        """
        CREATE TABLE IF NOT EXISTS users (
            id VARCHAR(50) PRIMARY KEY,
            name VARCHAR(100),
            password VARCHAR(100),
            level INT,
            login INT,
            recommend INT
        )
        """.trimIndent()
    )
    statement.close()
    connection.close()
}

fun main() {
    val context = AnnotationConfigApplicationContext(DataSourceConfig::class.java, DaoFactory::class.java)
    prepareDatabase(context)
    val dao = context.getBean("userDao", UserDaoJdbc::class.java)

    val user = User(
        id = "1",
        name = "John Doe",
        password = "password123"
    )
    dao.add(user)

    println("User added: $user")

    val user2 = dao.get(user.id)
    println("User retrieved: $user2")
}