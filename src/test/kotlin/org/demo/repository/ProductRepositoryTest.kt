package org.demo.repository

import org.demo.config.DatastoreConfig
import org.demo.model.Product
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest
import org.springframework.context.annotation.Import
import reactor.test.StepVerifier

/**
 * Integration tests for [ProductRepository].
 *
 * Uses [@DataR2dbcTest] to load only the R2DBC slice of the application context:
 * the repository, the connection factory, and the R2DBC auto-configuration.
 * [DatastoreConfig] is imported explicitly to run the schema and seed-data
 * initialisation scripts before tests execute.
 *
 * Learning note: [@DataR2dbcTest] is analogous to [@DataJpaTest] but for reactive
 * R2DBC repositories. It gives fast, focused tests without starting a web server
 * or loading unrelated beans.
 *
 * Learning note: [StepVerifier] is Project Reactor's test utility for asserting
 * on reactive streams step by step. It subscribes to the publisher under test and
 * verifies emissions, completion, and errors in a declarative, readable style.
 */
@DataR2dbcTest
@Import(DatastoreConfig::class)
class ProductRepositoryTest {

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Test
    fun `findAllProducts emits all three seeded products`() {
        StepVerifier.create(productRepository.findAllProducts())
            .expectNextCount(3)
            .verifyComplete()
    }

    @Test
    fun `findProductById returns the matching product`() {
        StepVerifier.create(productRepository.findProductById(1))
            .expectNextMatches { product ->
                product.id == 1 && product.name == "Wireless Headphones" && product.price == 79.99f
            }
            .verifyComplete()
    }

    @Test
    fun `findProductById returns empty Mono for an unknown id`() {
        // An empty Mono completes without emitting — StepVerifier.verifyComplete() asserts this.
        StepVerifier.create(productRepository.findProductById(999))
            .verifyComplete()
    }
}
