package ru.arendago

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier
import ru.arendago.dto.TaskRequest
import ru.arendago.model.TaskStatus
import ru.arendago.service.TaskService
import ru.arendago.exception.NotFoundException

@SpringBootTest
class TaskServiceTest(@Autowired val service: TaskService) {

    @BeforeEach
    fun cleanUp() {
        val all = service.streamAll(null).collectList().block() ?: emptyList()
        all.forEach { task ->
            service.deleteTask(task.id).block()
        }
    }

    @Test
    fun `успешное создание задачи`() {
        val request = TaskRequest("Test task", "Description")
        val mono = service.createTask(request)

        StepVerifier.create(mono)
            .assertNext { task ->
                assertNotNull(task.id)
                assertEquals(TaskStatus.NEW, task.status)
                assertEquals("Test task", task.title)
            }
            .verifyComplete()
    }

    @Test
    fun `получение задачи по id`() {
        val created = service.createTask(TaskRequest("Another task", null)).block()!!
        val mono = service.getTaskById(created.id)

        StepVerifier.create(mono)
            .assertNext { found ->
                assertEquals(created.id, found.id)
                assertEquals("Another task", found.title)
            }
            .verifyComplete()
    }

    @Test
    fun `ошибка при отсутствии задачи`() {
        val mono = service.getTaskById(9999)

        StepVerifier.create(mono)
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `обновление статуса`() {
        val created = service.createTask(TaskRequest("Status test", null)).block()!!
        val mono = service.updateStatus(created.id, TaskStatus.DONE)

        StepVerifier.create(mono)
            .assertNext { updated ->
                assertEquals(TaskStatus.DONE, updated.status)
                assertEquals(created.id, updated.id)
            }
            .verifyComplete()
    }

    @Test
    fun `удаление задачи`() {
        val created = service.createTask(TaskRequest("Delete test", null)).block()!!
        val deleteMono = service.deleteTask(created.id)

        StepVerifier.create(deleteMono)
            .verifyComplete()

        val afterDelete = service.getTaskById(created.id)
        StepVerifier.create(afterDelete)
            .expectError(NotFoundException::class.java)
            .verify()
    }

    @Test
    fun `получение списка задач с пагинацией и фильтрацией`() {
        service.createTask(TaskRequest("Task 1", null)).block()
        service.createTask(TaskRequest("Task 2", null)).block()
        service.createTask(TaskRequest("Task 3", null)).block()

        val page1 = service.getTasks(status = null, page = 0, size = 2)
        val page2 = service.getTasks(status = null, page = 1, size = 2)

        StepVerifier.create(page1)
            .assertNext { response ->
                assertEquals(2, response.content.size)
                assertEquals(3, response.totalElements)
                assertEquals(2, response.totalPages)
            }
            .verifyComplete()

        StepVerifier.create(page2)
            .assertNext { response ->
                assertEquals(1, response.content.size)
                assertEquals(3, response.totalElements)
                assertEquals(2, response.totalPages)
            }
            .verifyComplete()
    }
}
