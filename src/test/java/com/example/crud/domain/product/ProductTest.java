package com.example.crud.domain.product;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    @Test
    public void testConstructorAndGetters() {

        String name = "Test Product";
        Integer priceInCents = 100;
        RequestProduct requestProduct = new RequestProduct(null, name, priceInCents);

        Product product = new Product(requestProduct);

        assertEquals(name, product.getName());
        assertEquals(priceInCents, product.getPrice_in_cents());
        assertNull(product.getId());
    }

    @Test
    public void testEqualsAndHashCode() {

        String id = "1";
        String name = "Test Product";
        Integer priceInCents = 100;

        Product product1 = new Product(id, name, priceInCents);
        Product product2 = new Product(id, name, priceInCents);
        Product product3 = new Product("2", name, priceInCents);

        assertEquals(product1, product2);
        assertNotEquals(product1, product3);

        assertEquals(product1.hashCode(), product2.hashCode());
        assertNotEquals(product1.hashCode(), product3.hashCode());
    }
}
