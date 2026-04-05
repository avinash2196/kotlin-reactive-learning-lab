package org.demo.service

import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

/**
 * Service responsible for retrieving stock quantity data from the stock endpoint.
 *
 * In this learning project, the "stock service" is a mock endpoint hosted inside
 * the same application so the demo runs fully self-contained. In a real microservice
 * architecture this would be a separate deployed service with its own data store.
 *
 * Learning note: Isolating external HTTP calls into a dedicated service class improves
 * testability — you can mock [StockService] in controller tests instead of manually
 * stubbing the entire [WebClient] builder chain, which is brittle.
 *
 * Design note: [WebClient] is Spring's non-blocking, reactive HTTP client introduced
 * in Spring 5. It replaces the blocking [RestTemplate] for WebFlux applications.
 *
 * @property webClient Pre-configured [WebClient] instance (base URL set in WebClientConfiguration).
 */
@Service
class StockService(private val webClient: WebClient) {

    /**
     * Fetches the available stock quantity for a given product.
     *
     * Returns a [Mono] that emits the quantity once the (mock) stock endpoint
     * responds, without blocking any thread while waiting.
     *
     * @param productId The ID of the product to look up.
     * @return [Mono] emitting the stock count, or an error signal if the endpoint is unavailable.
     */
    fun getStockQuantity(productId: Int): Mono<Int> =
        webClient.get()
            .uri("/stock-service/product/$productId/quantity")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<Int>()
}
