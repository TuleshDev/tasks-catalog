package ru.arendago.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.arendago.dto.TaskRequest
import ru.arendago.dto.TaskResponse
import ru.arendago.dto.PageResponse
import ru.arendago.exception.NotFoundException
import ru.arendago.model.Task
import ru.arendago.model.TaskStatus
import ru.arendago.repository.TaskRepository
import java.time.LocalDateTime

@Service
class TaskService(private val repository: TaskRepository) {

    fun createTask(request: TaskRequest): Mono<TaskResponse> =
        Mono.fromCallable {
            val now = LocalDateTime.now()
            val task = Task(
                id = 0L,
                title = request.title,
                description = request.description,
                status = TaskStatus.NEW,
                createdAt = now,
                updatedAt = now
            )
            val generatedId = repository.saveAndReturnId(task)
            val saved = task.copy(id = generatedId)
            TaskResponse.from(saved)
        }

    fun getTaskById(id: Long): Mono<TaskResponse> =
        Mono.fromCallable {
            val task = repository.findById(id) ?: throw NotFoundException("Task $id not found")
            TaskResponse.from(task)
        }

    fun getTasks(status: TaskStatus?, page: Int, size: Int): Mono<PageResponse<TaskResponse>> =
        Mono.fromCallable {
            val (tasks, total) = repository.findAll(page, size, status)
            val content = tasks.map { TaskResponse.from(it) }
            val totalPages = if (size == 0) 0 else (total + size - 1) / size
            PageResponse(
                content = content,
                page = page,
                size = size,
                totalElements = total,
                totalPages = totalPages
            )
        }

    fun updateStatus(id: Long, status: TaskStatus): Mono<TaskResponse> =
        Mono.fromCallable {
            val task = repository.findById(id) ?: throw NotFoundException("Task $id not found")
            val updated = task.copy(status = status, updatedAt = LocalDateTime.now())
            repository.updateStatus(id, status)
            TaskResponse.from(updated)
        }

    fun deleteTask(id: Long): Mono<Void> =
        Mono.fromCallable {
            val rows = repository.delete(id)
            if (rows == 0) throw NotFoundException("Task $id not found")
            null
        }

    fun streamAll(status: TaskStatus?): Flux<TaskResponse> =
        Flux.defer {
            val (tasks, _) = repository.findAll(0, Int.MAX_VALUE, status)
            Flux.fromIterable(tasks.map { TaskResponse.from(it) })
        }
}
