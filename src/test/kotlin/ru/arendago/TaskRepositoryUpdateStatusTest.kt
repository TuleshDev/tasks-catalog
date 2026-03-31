package ru.arendago

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.mockito.kotlin.*
import ru.arendago.model.TaskStatus
import ru.arendago.repository.TaskRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec
import java.time.LocalDateTime

class TaskRepositoryUpdateStatusTest {

    private val jdbcClient: JdbcClient = mock()
    private val repository = TaskRepository(jdbcClient)

    @Test
    fun `updateStatus формирует SQL и обновляет задачу`() {
        val statementSpec: StatementSpec = mock()

        whenever(jdbcClient.sql(any<String>())).thenReturn(statementSpec)

        doAnswer { statementSpec }
            .whenever(statementSpec).param(any<String>(), anyOrNull())

        whenever(statementSpec.update()).thenReturn(1)

        val result = repository.updateStatus(1L, TaskStatus.DONE)

        assertEquals(1, result)

        verify(jdbcClient).sql(any<String>())
        verify(statementSpec).param("status", TaskStatus.DONE.name)
        verify(statementSpec).param(eq("updatedAt"), any<LocalDateTime>())
        verify(statementSpec).param("id", 1L)
        verify(statementSpec).update()
    }
}
