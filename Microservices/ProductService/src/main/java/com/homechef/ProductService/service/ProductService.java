package com.homechef.ProductService.service;


import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductService {

    ProductRepository productRepository;
    MongoClient mongoClient;

    @Autowired
    public ProductService(ProductRepository productRepository, MongoClient mongoClient) {
        this.productRepository=productRepository;
        this.mongoClient = mongoClient;
    }

    public Product createProduct(String name, Double price, UUID sellerId) {
        Product product = new Product.Builder(name, sellerId, price)
                .build();

        return productRepository.save(product);
    }



}
