package com.example.dao.user

import com.example.User
import com.example.dao.JdbcContext
import com.example.dao.StatementStrategy
import com.example.dao.user.statement.DeleteAllStatement
import com.example.dao.user.statement.dao.user.statement.AddStatement
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

/**
 * 1. DB 연결을 위한 Connection을 가져온다.
 * 2. SQL을 담은 Statement 를 만든다.
 * 3. 만들어진  Statement를 실행한다.
 * 4. 조회의 경우 SQL 쿼리의 실행결과를 `ResultSet` 으로 받아서 정보를 저장할 오브젝트에 옮겨준다.
 * 5. 작업 중에 생성된 `Connection`, `Statement`, `ResultSet` 같은 리소스는 작업을 마친 후 반드시 닫아준다.
 * 6. JDBC API가 만들어내는 예외를 잡아서 직접 처리하거나, 메소드에 throws를 선언해서 예외가 발생하면 메소드 밖으로 던지게 한다.
 */

class UserDao(
    private val dataSource: DataSource
) {
    private val jdbcTemplate: JdbcTemplate = JdbcTemplate(dataSource)

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun add(user: User) {
        jdbcTemplate.update(
            "INSERT INTO users(id, name, password) VALUES(?, ?, ?)",
            user.id, user.name, user.password
        )
    }

    @Throws(SQLException::class, ClassNotFoundException::class)
    fun get(id: String): User {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            { rs: ResultSet, _: Int ->
                User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password")
                )
            },
            id
        ) ?: throw EmptyResultDataAccessException(1)
    }

    @Throws(SQLException::class)
    fun deleteAll() {
        jdbcTemplate.update("DELETE FROM users")
    }


    @Throws(SQLException::class, ClassNotFoundException::class)
    fun getCount(): Int {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Int::class.java) ?: 0
    }

    fun getAll(): List<User> {
        return jdbcTemplate.query(
            "SELECT * FROM users ORDER BY id",
            { rs: ResultSet, _: Int ->
                User(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("password")
                )
            }
        )
    }
}