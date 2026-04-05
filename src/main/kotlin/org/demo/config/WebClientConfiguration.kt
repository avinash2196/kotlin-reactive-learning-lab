package org.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Spring configuration for the reactive HTTP client.
 *
 * Provides a singleton [WebClient] bean pre-configured with the application's
 * own base URL. All components that need to make outbound HTTP calls should
 * inject this bean rather than creating their own instances.
 *
 * Learning note: [WebClient] is the reactive, non-blocking replacement for
 * [org.springframework.web.client.RestTemplate] in Spring 5+. It integrates
 * directly with Project Reactor and supports streaming responses.
 *
 * Design note: The base URL points to `localhost:8080` so that [StockService]
 * can reach the mock stock endpoint hosted in the same application. In a real
 * architecture, each downstream service would have its own configured [WebClient]
 * (or use service discovery via an abstraction like Spring Cloud LoadBalancer).
 */
@Configuration
class WebClientConfiguration {

    /**
     * Creates a [WebClient] instance with `http://localhost:8080` as its base URL.
     *
     * Spring manages this as a singleton — one shared instance across the application,
     * which is safe because [WebClient] is thread-safe and designed for reuse.
     */
    @Bean
    fun webClient(): WebClient = WebClient.builder()
        .baseUrl("http://localhost:8080")
        .build()
}
