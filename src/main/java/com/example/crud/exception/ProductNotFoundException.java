package com.example.crud.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("Product with id '%s' was not found".formatted(id));
    }
}
