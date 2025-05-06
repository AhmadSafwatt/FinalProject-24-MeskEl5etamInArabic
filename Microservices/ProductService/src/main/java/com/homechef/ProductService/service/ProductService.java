package com.homechef.ProductService.service;


import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.model.ProductFactory;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

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

    public Product createProduct(String type, String name, UUID sellerId, Double price) {
        Product product = ProductFactory.createProduct(type, name, sellerId, price);
        return productRepository.save(product);
    }


}
