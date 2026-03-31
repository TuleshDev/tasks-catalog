package ru.arendago

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TasksCatalogApplication

fun main(args: Array<String>) {
    runApplication<TasksCatalogApplication>(*args)
}
