package com.example.crud.service;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void findAllMapsEntitiesToResponses() {
        when(repository.findAll()).thenReturn(List.of(new Product("A", 100), new Product("B", 200)));

        List<ProductResponse> result = service.findAll();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).name());
        assertEquals(200, result.get(1).priceInCents());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.findById("missing"));
    }

    @Test
    void createSavesNewProduct() {
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse result = service.create(new ProductRequest("New", 300));

        assertEquals("New", result.name());
        assertEquals(300, result.priceInCents());
        verify(repository).save(any(Product.class));
    }

    @Test
    void updateChangesExistingProduct() {
        Product existing = new Product("Old", 100);
        when(repository.findById("1")).thenReturn(Optional.of(existing));

        ProductResponse result = service.update("1", new ProductRequest("Updated", 150));

        assertEquals("Updated", result.name());
        assertEquals(150, existing.getPriceInCents());
    }

    @Test
    void deleteRemovesExistingProduct() {
        Product existing = new Product("A", 100);
        when(repository.findById("1")).thenReturn(Optional.of(existing));

        service.delete("1");

        verify(repository).delete(existing);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> service.delete("missing"));
        verify(repository, never()).delete(any());
    }
}
