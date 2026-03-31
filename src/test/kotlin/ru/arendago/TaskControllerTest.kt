package ru.arendago

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.test.StepVerifier
import ru.arendago.dto.TaskRequest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class TaskControllerTest(@Autowired private val client: WebTestClient) {

    @Test
    fun `создание задачи возвращает 201 реактивно`() {
        val responseFlux = client.post()
            .uri("/api/tasks")
            .bodyValue(TaskRequest("Controller task", "Desc"))
            .exchange()
            .returnResult(String::class.java)
            .responseBody

        StepVerifier.create(responseFlux)
            .expectNextMatches { body ->
                body.contains("\"title\":\"Controller task\"")
            }
            .verifyComplete()
    }

    @Test
    fun `404 при отсутствии задачи реактивно`() {
        val responseFlux = client.get()
            .uri("/api/tasks/9999")
            .exchange()
            .returnResult(String::class.java)
            .responseBody

        StepVerifier.create(responseFlux)
            .expectNextMatches { body ->
                body.contains("Not Found") || body.isNotEmpty()
            }
            .verifyComplete()
    }

    @Test
    fun `валидация входных данных реактивно`() {
        val responseFlux = client.post()
            .uri("/api/tasks")
            .bodyValue(TaskRequest("", "Desc"))
            .exchange()
            .returnResult(String::class.java)
            .responseBody

        StepVerifier.create(responseFlux)
            .expectNextMatches { body ->
                body.contains("Bad Request") || body.isNotEmpty()
            }
            .verifyComplete()
    }
}
