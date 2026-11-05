package com.example.crud.domain.product;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductTest {

    @Test
    void constructorSetsFieldsAndLeavesIdEmpty() {
        Product product = new Product("Test Product", 100);

        assertEquals("Test Product", product.getName());
        assertEquals(100, product.getPriceInCents());
        assertNull(product.getId());
    }

    @Test
    void updateReplacesNameAndPrice() {
        Product product = new Product("Old", 100);

        product.update("New", 250);

        assertEquals("New", product.getName());
        assertEquals(250, product.getPriceInCents());
    }

    @Test
    void equalityIsBasedOnId() {
        Product first = productWithId("1", "A", 100);
        Product sameId = productWithId("1", "B", 200);
        Product otherId = productWithId("2", "A", 100);

        assertEquals(first, sameId);
        assertEquals(first.hashCode(), sameId.hashCode());
        assertNotEquals(first, otherId);
    }

    @Test
    void transientProductsAreNotEqual() {
        assertNotEquals(new Product("A", 100), new Product("A", 100));
    }

    private static Product productWithId(String id, String name, int priceInCents) {
        Product product = new Product(name, priceInCents);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
