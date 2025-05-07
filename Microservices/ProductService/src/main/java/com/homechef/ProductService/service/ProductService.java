package com.homechef.ProductService.service;


import com.homechef.ProductService.model.BeverageFactory;
import com.homechef.ProductService.model.FoodFactory;
import com.homechef.ProductService.model.Product;
import com.homechef.ProductService.model.ProductFactory;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
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
//        Product product = ProductFactory.createProduct(type, name, sellerId, price, amountSold);
        ProductFactory factory;

        switch (type.toLowerCase()) {
            case "food":
                factory = new FoodFactory();
                break;
            case "beverage":
                factory = new BeverageFactory();
                break;
            default:
                throw new IllegalArgumentException("Unknown product type: " + type);
        }

        Product product = factory.createProduct(name, sellerId, price, amountSold);
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        UUID productUUID = UUID.fromString(id);
        return productRepository.findById(productUUID).orElse(null);
    }

    public Product updateProduct(String id,String name, Double price, int amountSold){
        MongoDatabase mongoDatabase= this.mongoClient.getDatabase("elthon2yelamr7");
        MongoCollection<Document> products = mongoDatabase.getCollection("products");


        UUID productUUID = UUID.fromString(id);
        Optional<Product> productOptional = productRepository.findById(productUUID);

        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Bson updateOperation = Updates.combine(
                Updates.set("name", name),
                Updates.set("price", price),
                Updates.set("amountSold",amountSold)
        );

        products.updateOne(
                Filters.eq("_id", id),
                updateOperation
        );


        Optional<Product> updatedProduct = productRepository.findById(productUUID);
        return updatedProduct.get();

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
