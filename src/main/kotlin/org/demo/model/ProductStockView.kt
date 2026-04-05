package org.demo.model

/**
 * A composite read-model that combines product data with its current stock level.
 *
 * This is a View Model (DTO) — it aggregates data from two distinct sources: the
 * local product database and the remote stock service. It is assembled on demand
 * and never persisted.
 *
 * Learning note: This pattern mirrors the **API Composition** pattern used in
 * microservice architectures, where a BFF (Backend for Frontend) or gateway merges
 * data from multiple upstream services into a single response.
 *
 * Design note: Modelled as a read-only [data class] because it is constructed once
 * per request and never mutated. The [from] factory keeps construction logic in one place.
 *
 * @property id             Product identifier.
 * @property name           Product display name.
 * @property price          Product unit price.
 * @property stockQuantity  Current number of units available in the warehouse.
 */
data class ProductStockView(
    val id: Int,
    val name: String,
    val price: Float,
    val stockQuantity: Int
) {
    companion object {
        /**
         * Assembles a [ProductStockView] from a [Product] and its stock count.
         *
         * Using a factory method keeps the controller lean — it calls `from(product, qty)`
         * rather than constructing the DTO inline.
         */
        fun from(product: Product, stockQuantity: Int) = ProductStockView(
            id = product.id ?: 0,
            name = product.name,
            price = product.price,
            stockQuantity = stockQuantity
        )
    }
}
