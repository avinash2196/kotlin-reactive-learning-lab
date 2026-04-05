package org.demo.repository

import org.demo.model.Product
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Reactive repository for [Product] entities, backed by R2DBC.
 *
 * Extends [ReactiveCrudRepository] to inherit standard CRUD operations as
 * non-blocking [Mono]/[Flux] publishers. Custom queries are written in native
 * SQL and bound via Spring Data's [@Query] annotation.
 *
 * Learning note: R2DBC (Reactive Relational Database Connectivity) provides a
 * fully non-blocking database API. Unlike JDBC, no thread is blocked while
 * waiting for a database response — enabling higher throughput with fewer threads.
 *
 * Design note: Queries live here rather than in the controller, following the
 * single-responsibility principle and making them easy to test in isolation
 * with [@DataR2dbcTest][org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest].
 */
@Repository
interface ProductRepository : ReactiveCrudRepository<Product, Int> {

    /**
     * Retrieves a single [Product] by its primary key.
     * Returns an empty [Mono] if no product with the given [id] exists.
     */
    @Query("SELECT * FROM products WHERE id = :id")
    fun findProductById(id: Int): Mono<Product>

    /**
     * Retrieves all products as a reactive stream.
     *
     * Items are emitted one by one as they arrive from the database — no need
     * to wait for the entire result set before processing begins.
     */
    @Query("SELECT * FROM products")
    fun findAllProducts(): Flux<Product>
}

