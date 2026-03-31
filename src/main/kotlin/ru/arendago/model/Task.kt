package ru.arendago.model

import java.time.LocalDateTime

data class Task(
    val id: Long,
    val title: String,
    val description: String? = null,
    var status: TaskStatus = TaskStatus.NEW,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
