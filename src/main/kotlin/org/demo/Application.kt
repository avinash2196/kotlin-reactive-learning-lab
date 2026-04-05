package org.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Entry point for the Kotlin Reactive Learning Lab.
 *
 * This Spring Boot application demonstrates end-to-end reactive programming with:
 * - **Spring WebFlux** — non-blocking HTTP layer using Project Reactor
 * - **R2DBC** — reactive, non-blocking access to a relational database
 * - **WebClient** — reactive HTTP client for calling external services
 * - **Mono / Flux composition** — combining multiple async data sources
 *
 * Learning note: `runApplication<Application>(*args)` is the idiomatic Kotlin
 * equivalent of `SpringApplication.run(Application::class.java, *args)`.
 */
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
