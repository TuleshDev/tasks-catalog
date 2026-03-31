package ru.arendago.controller

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import ru.arendago.dto.TaskRequest
import ru.arendago.dto.TaskResponse
import ru.arendago.dto.PageResponse
import ru.arendago.model.TaskStatus
import ru.arendago.service.TaskService

@RestController
@RequestMapping("/api/tasks")
class TaskController(private val service: TaskService) {

    @PostMapping
    fun createTask(@RequestBody request: TaskRequest): Mono<TaskResponse> =
        service.createTask(request)

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): Mono<TaskResponse> =
        service.getTaskById(id)

    @GetMapping
    fun getTasks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) status: TaskStatus?
    ): Mono<PageResponse<TaskResponse>> =
        service.getTasks(status, page, size)

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody body: Map<String, String>
    ): Mono<TaskResponse> {
        val status = TaskStatus.valueOf(body["status"] ?: throw IllegalArgumentException("status is required"))
        return service.updateStatus(id, status)
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long): Mono<Void> =
        service.deleteTask(id)

    @GetMapping("/stream")
    fun streamAll(@RequestParam(required = false) status: TaskStatus?): Flux<TaskResponse> =
        service.streamAll(status)
}
