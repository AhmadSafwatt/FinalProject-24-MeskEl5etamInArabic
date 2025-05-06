package com.homechef.ProductService.service;


import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.model.ProductFactory;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    ProductRepository productRepository;
    MongoClient mongoClient;

    @Autowired
    public ProductService(ProductRepository productRepository, MongoClient mongoClient) {
        this.productRepository=productRepository;
        this.mongoClient = mongoClient;
    }

    public Product createProduct(String type, String name, UUID sellerId, Double price, int amountSold) {
        Product product = ProductFactory.createProduct(type, name, sellerId, price, amountSold);
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        UUID productUUID = UUID.fromString(id);
        return productRepository.findById(productUUID).orElse(null);
    }

    public List<Product> getMostSoldProducts() {
        int maxAmountSold = productRepository.findAll()
                .stream()
                .mapToInt(Product::getAmountSold)
                .max()
                .orElse(0);

        return productRepository.findAll()
                .stream()
                .filter(product -> product.getAmountSold() == maxAmountSold)
                .collect(Collectors.toList());
    }

    public void deleteProductById(String id) {
        UUID productUUID = UUID.fromString(id);
        productRepository.deleteById(productUUID);
    }

}
