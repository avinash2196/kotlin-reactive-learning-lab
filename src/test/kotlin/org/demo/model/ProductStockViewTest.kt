package org.demo.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ProductStockView].
 *
 * Pure unit tests — no Spring context needed, just plain JUnit 5.
 */
class ProductStockViewTest {

    @Test
    fun `from factory method correctly maps all product fields and stock quantity`() {
        val product = Product(id = 2, name = "Mechanical Keyboard", price = 129.99f)
        val stockQty = 15

        val view = ProductStockView.from(product, stockQty)

        assertEquals(2, view.id)
        assertEquals("Mechanical Keyboard", view.name)
        assertEquals(129.99f, view.price)
        assertEquals(15, view.stockQuantity)
    }

    @Test
    fun `from factory method handles zero stock quantity`() {
        val product = Product(id = 1, name = "Wireless Headphones", price = 79.99f)

        val view = ProductStockView.from(product, 0)

        assertEquals(0, view.stockQuantity)
    }

    @Test
    fun `data class equality holds for identical views`() {
        val product = Product(id = 3, name = "USB-C Hub", price = 49.99f)

        val view1 = ProductStockView.from(product, 5)
        val view2 = ProductStockView.from(product, 5)

        assertEquals(view1, view2)
    }
}
