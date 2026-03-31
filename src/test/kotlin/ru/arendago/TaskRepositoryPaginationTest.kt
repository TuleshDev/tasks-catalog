package ru.arendago

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.simple.JdbcClient
import ru.arendago.repository.TaskRepository
import ru.arendago.model.Task
import ru.arendago.model.TaskStatus
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class TaskRepositoryPaginationTest(
    @Autowired private val repository: TaskRepository,
    @Autowired private val jdbcClient: JdbcClient
) {

    @BeforeEach
    fun setup() {
        jdbcClient.sql("DELETE FROM tasks").update()

        val now = LocalDateTime.now()
        repository.saveAndReturnId(Task(0L, "Prepare report", "Desc1", TaskStatus.IN_PROGRESS, now, now))
        repository.saveAndReturnId(Task(0L, "Write code", "Desc2", TaskStatus.NEW, now, now))
        repository.saveAndReturnId(Task(0L, "Fix bugs", "Desc3", TaskStatus.NEW, now, now))
        repository.saveAndReturnId(Task(0L, "Deploy app", "Desc4", TaskStatus.DONE, now, now))
    }

    @Test
    fun `findAll возвращает все задачи`() {
        val (tasks, total) = repository.findAll(page = 0, size = 10, status = null)

        assertEquals(4, total)
        assertTrue(tasks.any { it.title == "Prepare report" })
        assertTrue(tasks.any { it.status == TaskStatus.IN_PROGRESS })
    }

    @Test
    fun `findAll с фильтром по статусу`() {
        val (tasks, total) = repository.findAll(page = 0, size = 10, status = TaskStatus.NEW)

        assertEquals(2, total)
        assertTrue(tasks.all { it.status == TaskStatus.NEW })
    }
}
