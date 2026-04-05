package org.demo.controller

import org.demo.model.Product
import org.demo.model.ProductStockView
import org.demo.repository.ProductRepository
import org.demo.service.StockService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.test.web.reactive.server.expectBodyList
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Unit tests for [ProductController].
 *
 * Uses [@WebFluxTest] to spin up only the WebFlux layer without loading the full
 * application context. Repository and service dependencies are replaced by Mockito
 * mocks via [@MockBean], so tests run fast and in complete isolation.
 *
 * Learning note: [@WebFluxTest] is the reactive equivalent of [@WebMvcTest].
 * It auto-configures [WebTestClient] for making test HTTP requests and asserting
 * on responses without starting a real HTTP server.
 */
@WebFluxTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var client: WebTestClient

    @MockBean
    private lateinit var productRepository: ProductRepository

    @MockBean
    private lateinit var stockService: StockService

    private val sampleProducts = listOf(
        Product(1, "Wireless Headphones", 79.99f),
        Product(2, "Mechanical Keyboard", 129.99f),
        Product(3, "USB-C Hub", 49.99f)
    )

    @Test
    fun `GET products returns all products`() {
        given(productRepository.findAllProducts())
            .willReturn(Flux.fromIterable(sampleProducts))

        client.get()
            .uri("/products")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Product>()
            .hasSize(3)
    }

    @Test
    fun `GET products by id returns the correct product`() {
        val product = sampleProducts[0]
        given(productRepository.findProductById(1)).willReturn(Mono.just(product))

        client.get()
            .uri("/products/1")
            .exchange()
            .expectStatus().isOk
            .expectBody<Product>()
            .isEqualTo(product)
    }

    @Test
    fun `GET products by id returns 404 when product not found`() {
        given(productRepository.findProductById(99)).willReturn(Mono.empty())

        client.get()
            .uri("/products/99")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `GET products stock view combines product and stock data`() {
        val product = sampleProducts[0]
        given(productRepository.findProductById(1)).willReturn(Mono.just(product))
        given(stockService.getStockQuantity(1)).willReturn(Mono.just(10))

        val expected = ProductStockView.from(product, 10)

        client.get()
            .uri("/products/1/stock")
            .exchange()
            .expectStatus().isOk
            .expectBody<ProductStockView>()
            .isEqualTo(expected)
    }

    @Test
    fun `GET products stock view returns 404 when product not found`() {
        given(productRepository.findProductById(99)).willReturn(Mono.empty())
        given(stockService.getStockQuantity(99)).willReturn(Mono.just(10))

        client.get()
            .uri("/products/99/stock")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `GET mock stock endpoint returns fixed quantity`() {
        client.get()
            .uri("/stock-service/product/1/quantity")
            .exchange()
            .expectStatus().isOk
            .expectBody<Int>()
            .isEqualTo(10)
    }
}
