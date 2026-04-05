package org.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

/**
 * Domain model representing a product in the catalog.
 *
 * Maps to the PRODUCTS table via R2DBC. Using a [data class] gives us structural
 * equality, [copy] semantics, and a useful [toString] — all valuable when tracing
 * items through reactive streams.
 *
 * Learning note: In reactive pipelines, domain objects flow through Mono/Flux operators.
 * Keeping them immutable (val fields) prevents accidental mutation across threads,
 * though R2DBC currently requires a default constructor so defaults are provided.
 *
 * @property id  Auto-generated primary key. `null` before the entity is first persisted;
 *               Spring Data R2DBC uses null to detect new entities and omit the ID in INSERT.
 * @property name  Display name of the product.
 * @property price  Unit price in the default currency (no currency code for simplicity).
 */
@Table("PRODUCTS")
data class Product(
    @Id val id: Int? = null,
    val name: String = "",
    val price: Float = 0.0f
)
