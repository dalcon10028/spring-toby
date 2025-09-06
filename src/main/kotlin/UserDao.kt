package com.example

import java.sql.*

/**
 * 1. DB 연결을 위한 Connection을 가져온다.
 * 2. SQL을 담은 Statement 를 만든다.
 * 3. 만들어진  Statement를 실행한다.
 * 4. 조회의 경우 SQL 쿼리의 실행결과를 `ResultSet` 으로 받아서 정보를 저장할 오브젝트에 옮겨준다.
 * 5. 작업 중에 생성된 `Connection`, `Statement`, `ResultSet` 같은 리소스는 작업을 마친 후 반드시 닫아준다.
 * 6. JDBC API가 만들어내는 예외를 잡아서 직접 처리하거나, 메소드에 throws를 선언해서 예외가 발생하면 메소드 밖으로 던지게 한다.
 */

class UserDao(
    private val connectionMaker: SimpleConnectionMaker,
) {
    @Throws(SQLException::class, ClassNotFoundException::class)
    fun add(user: User) {
        val connection = connectionMaker.makeNewConnection()
        val ps = connection.prepareStatement("INSERT INTO users(id, name, password) VALUES(?, ?, ?)")
        ps.setString(1, user.id)
        ps.setString(2, user.name)
        ps.setString(3, user.password)
        ps.executeUpdate()
        ps.close()
        connection.close()
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun get(id: String): User {
        val connection = connectionMaker.makeNewConnection()
        val ps = connection.prepareStatement("SELECT * FROM users WHERE id = ?")
        ps.setString(1, id)

        val rs = ps.executeQuery()
        rs.next()
        val user = User(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("password")
        )

        rs.close()
        ps.close()
        connection.close()

        return user
    }
}