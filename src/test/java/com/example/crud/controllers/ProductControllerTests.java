package com.example.crud.controllers;

import com.example.crud.domain.product.Product;
import com.example.crud.domain.product.ProductRepository;
import com.example.crud.domain.product.RequestProduct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductRepository productRepository;

    @Test
    public void testGetAllProducts() throws Exception {

        List<Product> productList = new ArrayList<>();
        productList.add(new Product("1", "Product 1", 100));
        productList.add(new Product("2", "Product 2", 200));
        when(productRepository.findAll()).thenReturn(productList);

        mockMvc.perform(MockMvcRequestBuilders.get("/product"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Product 1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].name").value("Product 2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].price_in_cents").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].price_in_cents").value(200));
    }

    @Test
    public void testGetProduct() throws Exception {

        Product product = new Product("1", "Test Product", 150);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        mockMvc.perform(MockMvcRequestBuilders.get("/product/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Product"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price_in_cents").value(150));
    }

    @Test
    public void testRegisterProduct() throws Exception {

        RequestProduct requestProduct = new RequestProduct("Test Product", "Test Product", 200);
        String requestJson = "{ \"name\": \"Test Product\", \"price_in_cents\": 200 }";

        mockMvc.perform(MockMvcRequestBuilders.post("/product")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    public void testUpdateProduct() throws Exception {

        Product product = new Product("1", "Product", 100);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        RequestProduct requestData = new RequestProduct("1", "Updated Product", 150);
        String requestJson = "{ \"name\": \"Updated Product\", \"price_in_cents\": 150 }";

        mockMvc.perform(MockMvcRequestBuilders.put("/product/1")
                        .contentType("application/json")
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Product"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.price_in_cents").value(150));
    }

    @Test
    public void testDeleteProduct() throws Exception {

        Product product = new Product("1", "Test Product", 200);
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        mockMvc.perform(MockMvcRequestBuilders.delete("/product/1"))
                .andExpect(status().isOk());
    }



}
