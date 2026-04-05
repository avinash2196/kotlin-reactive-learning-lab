package org.demo.controller

import org.demo.model.Product
import org.demo.model.ProductStockView
import org.demo.repository.ProductRepository
import org.demo.service.StockService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * REST controller exposing the Product API.
 *
 * Handles incoming HTTP requests and delegates data access to [ProductRepository].
 * For the stock-enriched view it composes two concurrent async calls using
 * reactive operators — no thread is ever blocked waiting for either result.
 *
 * Learning note: Spring WebFlux allows controller methods to return reactive types
 * ([Mono]/[Flux]) directly. The framework subscribes to these publishers
 * and streams their emissions back to the HTTP client without occupying a thread
 * while waiting.
 *
 * Architecture note: This controller intentionally has no dedicated service layer
 * (beyond the repository and [StockService]) to keep the example focused. A
 * production application would typically introduce a service class to isolate
 * business rules from transport concerns.
 *
 * @property productRepository  Reactive repository for database access.
 * @property stockService       Service for fetching stock quantities from the stock endpoint.
 */
@RestController
class ProductController(
    private val productRepository: ProductRepository,
    private val stockService: StockService
) {

    /**
     * Returns all products as a reactive stream.
     *
     * [Flux] emits items one by one as they are read from the database, enabling
     * backpressure and memory-efficient streaming for large result sets.
     *
     * `GET /products`
     */
    @GetMapping("/products")
    fun findAll(): Flux<Product> = productRepository.findAllProducts()

    /**
     * Returns a single product by its ID.
     *
     * [Mono] represents a 0-or-1 result. If no product exists for the given [id],
     * a 404 Not Found response is returned via [ResponseStatusException].
     *
     * Learning note: `.switchIfEmpty(Mono.error(...))` is the idiomatic reactive
     * way to map an empty publisher to an error signal.
     *
     * `GET /products/{id}`
     */
    @GetMapping("/products/{id}")
    fun findById(@PathVariable id: Int): Mono<Product> =
        productRepository.findProductById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $id")))

    /**
     * Returns a product enriched with its current stock quantity.
     *
     * Demonstrates **reactive composition**: the product lookup and the stock
     * quantity fetch are two independent async operations that run concurrently.
     * [Mono.zipWith] assembles the final result once both complete — similar to
     * `Promise.all` in JavaScript or `CompletableFuture.thenCombine` in Java.
     *
     * Architecture note: This mirrors the **API Composition** pattern in microservices,
     * where a BFF aggregates data from multiple upstream services into one response.
     * The trade-off is increased latency coupling — if the stock service is slow,
     * this endpoint is slow too.
     *
     * `GET /products/{id}/stock`
     */
    @GetMapping("/products/{id}/stock")
    fun findByIdWithStock(@PathVariable id: Int): Mono<ProductStockView> {
        val product = productRepository.findProductById(id)
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: $id")))
        val stockQuantity = stockService.getStockQuantity(id)
        return product.zipWith(stockQuantity) { p, qty -> ProductStockView.from(p, qty) }
    }

    // -------------------------------------------------------------------------
    // Mock stock endpoint
    // -------------------------------------------------------------------------

    /**
     * Mock endpoint that simulates an external stock microservice.
     *
     * Always returns a fixed quantity of **10** units, regardless of the product ID.
     * This allows the demo to run fully standalone without any external dependencies.
     *
     * Learning note: In a real system this would be a separate deployed service
     * with its own database. Hosting the mock inside the same application lets
     * us demonstrate reactive API composition patterns without standing up
     * additional infrastructure.
     *
     * `GET /stock-service/product/{id}/quantity`
     */
    @GetMapping("/stock-service/product/{id}/quantity")
    fun getMockStockQuantity(@PathVariable id: Int): Mono<Int> = Mono.just(10)
}
