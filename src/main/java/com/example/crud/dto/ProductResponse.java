package com.example.crud.dto;

import com.example.crud.domain.product.Product;

public record ProductResponse(String id, String name, Integer priceInCents) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getName(), product.getPriceInCents());
    }
}
