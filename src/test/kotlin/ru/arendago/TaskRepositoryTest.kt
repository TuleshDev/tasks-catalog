package ru.arendago

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import ru.arendago.model.Task
import ru.arendago.model.TaskStatus
import ru.arendago.repository.TaskRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec
import org.springframework.jdbc.core.simple.JdbcClient.MappedQuerySpec
import java.time.LocalDateTime

class TaskRepositoryTest {

    private val jdbcClient: JdbcClient = mock()
    private val repository = TaskRepository(jdbcClient)

    @Test
    fun `findById формирует SQL и возвращает задачу`() {
        val now = LocalDateTime.now()
        val expected = Task(1L, "Repo test", "Desc", TaskStatus.NEW, now, now)

        val statementSpec: StatementSpec = mock()
        val querySpec: MappedQuerySpec<Task> = mock()

        whenever(jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")).thenReturn(statementSpec)
        doReturn(statementSpec).`when`(statementSpec).param(eq("id"), eq(1L))
        whenever(statementSpec.query(Task::class.java)).thenReturn(querySpec)
        whenever(querySpec.list()).thenReturn(listOf(expected))

        val result = repository.findById(1L)

        assertEquals(expected, result)
    }

    @Test
    fun `findById возвращает null если задачи нет`() {
        val statementSpec: StatementSpec = mock()
        val querySpec: MappedQuerySpec<Task> = mock()

        whenever(jdbcClient.sql("SELECT * FROM tasks WHERE id = :id")).thenReturn(statementSpec)
        doReturn(statementSpec).`when`(statementSpec).param(eq("id"), eq(99L))
        whenever(statementSpec.query(Task::class.java)).thenReturn(querySpec)
        whenever(querySpec.list()).thenReturn(emptyList())

        val result = repository.findById(99L)

        assertNull(result)
    }

    @Test
    fun `findAll формирует SQL с LIMIT OFFSET и возвращает Pair`() {
        val now = LocalDateTime.now()
        val tasks = listOf(
            Task(1L, "Task 1", "Desc1", TaskStatus.NEW, now, now),
            Task(2L, "Task 2", "Desc2", TaskStatus.NEW, now, now)
        )

        val statementSpecTasks: StatementSpec = mock()
        val querySpecTasks: MappedQuerySpec<Task> = mock()
        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpecTasks)
        doReturn(statementSpecTasks).`when`(statementSpecTasks).param(any<String>(), anyOrNull())
        whenever(statementSpecTasks.query(Task::class.java)).thenReturn(querySpecTasks)
        whenever(querySpecTasks.list()).thenReturn(tasks)

        val statementSpecCount: StatementSpec = mock()
        val querySpecCount: MappedQuerySpec<Long> = mock()
        whenever(jdbcClient.sql("SELECT COUNT(*) FROM tasks")).thenReturn(statementSpecCount)
        whenever(statementSpecCount.query(Long::class.java)).thenReturn(querySpecCount)
        whenever(querySpecCount.single()).thenReturn(2L)

        val result = repository.findAll(page = 0, size = 2, status = null)

        assertEquals(2, result.first.size)
        assertEquals("Task 1", result.first[0].title)
        assertEquals("Task 2", result.first[1].title)
        assertEquals(2L, result.second)
    }
}
