package ru.arendago.repository

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import ru.arendago.model.Task
import ru.arendago.model.TaskStatus
import java.time.LocalDateTime

@Repository
class TaskRepository(private val jdbcClient: JdbcClient) {

    fun findById(id: Long): Task? =
        jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")
            .param("id", id)
            .query(Task::class.java)
            .list()
            .singleOrNull()

    fun findAll(page: Int, size: Int, status: TaskStatus?): Pair<List<Task>, Long> {
        val offset = page * size

        val baseSql = StringBuilder("SELECT * FROM tasks")
        val countSql = StringBuilder("SELECT COUNT(*) FROM tasks")

        if (status != null) {
            baseSql.append(" WHERE status = :status")
            countSql.append(" WHERE status = :status")
        }

        baseSql.append(" ORDER BY created_at DESC LIMIT :limit OFFSET :offset")

        val tasks = jdbcClient.sql(baseSql.toString())
            .apply {
                if (status != null) param("status", status.name)
                param("limit", size)
                param("offset", offset)
            }
            .query(Task::class.java)
            .list()

        val total = jdbcClient.sql(countSql.toString())
            .apply {
                if (status != null) param("status", status.name)
            }
            .query(Long::class.java)
            .single()

        return tasks to total
    }

    fun saveAndReturnId(task: Task): Long =
        jdbcClient.sql(
            """INSERT INTO tasks (title, description, status, created_at, updated_at) 
               VALUES (:title, :description, :status, :createdAt, :updatedAt) 
               RETURNING id"""
        )
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("createdAt", task.createdAt)
            .param("updatedAt", task.updatedAt)
            .query(Long::class.java)
            .single()

    fun update(task: Task): Int =
        jdbcClient.sql(
            """UPDATE tasks 
               SET title = :title, description = :description, status = :status, updated_at = :updatedAt 
               WHERE id = :id"""
        )
            .param("title", task.title)
            .param("description", task.description)
            .param("status", task.status.name)
            .param("updatedAt", LocalDateTime.now())
            .param("id", task.id)
            .update()

    fun updateStatus(id: Long, status: TaskStatus): Int =
        jdbcClient.sql(
            """UPDATE tasks 
               SET status = :status, updated_at = :updatedAt 
               WHERE id = :id"""
        )
            .param("status", status.name)
            .param("updatedAt", LocalDateTime.now())
            .param("id", id)
            .update()

    fun delete(id: Long): Int =
        jdbcClient.sql("DELETE FROM tasks WHERE id = :id")
            .param("id", id)
            .update()
}
