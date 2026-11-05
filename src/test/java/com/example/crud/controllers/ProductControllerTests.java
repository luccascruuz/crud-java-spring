package com.example.crud.controllers;

import com.example.crud.dto.ProductRequest;
import com.example.crud.dto.ProductResponse;
import com.example.crud.exception.ProductNotFoundException;
import com.example.crud.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService service;

    @Test
    void getAllProductsReturnsList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                new ProductResponse("1", "Product 1", 100),
                new ProductResponse("2", "Product 2", 200)));

        mockMvc.perform(get("/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Product 1"))
                .andExpect(jsonPath("$[1].price_in_cents").value(200));
    }

    @Test
    void getProductReturnsProduct() throws Exception {
        when(service.findById("1")).thenReturn(new ProductResponse("1", "Test Product", 150));

        mockMvc.perform(get("/product/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price_in_cents").value(150));
    }

    @Test
    void getProductReturnsNotFoundWhenMissing() throws Exception {
        when(service.findById("missing")).thenThrow(new ProductNotFoundException("missing"));

        mockMvc.perform(get("/product/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"));
    }

    @Test
    void registerProductReturnsCreated() throws Exception {
        when(service.create(new ProductRequest("Test Product", 200)))
                .thenReturn(new ProductResponse("abc", "Test Product", 200));

        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Test Product\", \"price_in_cents\": 200 }"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/product/abc"))
                .andExpect(jsonPath("$.id").value("abc"));
    }

    @Test
    void registerProductRejectsInvalidBody() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"\", \"price_in_cents\": -1 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price_in_cents").exists());

        verify(service, never()).create(any());
    }

    @Test
    void updateProductReturnsUpdatedProduct() throws Exception {
        when(service.update(eq("1"), any(ProductRequest.class)))
                .thenReturn(new ProductResponse("1", "Updated Product", 150));

        mockMvc.perform(put("/product/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"Updated Product\", \"price_in_cents\": 150 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"))
                .andExpect(jsonPath("$.price_in_cents").value(150));
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/product/1"))
                .andExpect(status().isNoContent());

        verify(service).delete("1");
    }

    @Test
    void deleteProductReturnsNotFoundWhenMissing() throws Exception {
        doThrow(new ProductNotFoundException("missing")).when(service).delete("missing");

        mockMvc.perform(delete("/product/missing"))
                .andExpect(status().isNotFound());
    }
}
