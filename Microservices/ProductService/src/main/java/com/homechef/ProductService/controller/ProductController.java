package com.homechef.ProductService.controller;


import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductService productService;


    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product createProduct(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String name = (String) request.get("name");
        Double price = ((Number) request.get("price")).doubleValue();
        UUID sellerId = UUID.fromString((String) request.get("sellerId"));

        return productService.createProduct(type, name, sellerId, price);
    }

}
