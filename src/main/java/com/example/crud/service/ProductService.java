package com.example.crud.service;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return repository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(String id) {
        return ProductResponse.from(getExisting(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.priceInCents());
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest request) {
        Product product = getExisting(id);
        product.update(request.name(), request.priceInCents());
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(String id) {
        repository.delete(getExisting(id));
    }

    private Product getExisting(String id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}
