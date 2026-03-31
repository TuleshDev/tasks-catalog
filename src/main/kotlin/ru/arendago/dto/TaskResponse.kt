package ru.arendago.dto

import ru.arendago.model.Task
import ru.arendago.model.TaskStatus
import java.time.LocalDateTime

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(task: Task): TaskResponse =
            TaskResponse(
                id = task.id ?: throw IllegalStateException("Task id must not be null"),
                title = task.title,
                description = task.description,
                status = task.status,
                createdAt = task.createdAt,
                updatedAt = task.updatedAt
            )
    }
}
