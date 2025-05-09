package com.homechef.ProductService.service;


import com.homechef.ProductService.model.*;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {

    ProductRepository productRepository;
    MongoClient mongoClient;
    MongoTemplate mongoTemplate;

    @Autowired
    public ProductService(ProductRepository productRepository, MongoClient mongoClient, MongoTemplate mongoTemplate) {
        this.productRepository=productRepository;
        this.mongoClient = mongoClient;
        this.mongoTemplate = mongoTemplate;
    }

    public Product createProduct(String type, String name, UUID sellerId, Double price, int amountSold, String description, Double discount, Map<String, Object> request) {
        // Validate the input parameters
        if (type == null || name == null || sellerId == null || price == null || amountSold < 0) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        // Create the product using the factory method
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

        Product product = factory.createProduct(name, sellerId, price, amountSold,description,discount,request);
        return productRepository.save(product);
    }

    public Product getProductById(String id) {
        UUID productUUID = UUID.fromString(id);
        return productRepository.findById(productUUID).orElse(null);
    }

    public List<Product> getProductsById(List<String> ids) {
        List<Product> products = new ArrayList<>();
        for (String id : ids){
            UUID productUUID = UUID.fromString(id);
            Product product = productRepository.findById(productUUID).orElse(null);
            products.add(product);
        }
        return products;
    }

//    public List<Product> getMostSoldProducts() {
//        int maxAmountSold = productRepository.findAll()
//                .stream()
//                .mapToInt(Product::getAmountSold)
//                .max()
//                .orElse(0);
//
//        return productRepository.findAll()
//                .stream()
//                .filter(product -> product.getAmountSold() == maxAmountSold)
//                .collect(Collectors.toList());
//    }

    public List<Product> getMostSoldProducts() {
        Product topProduct = productRepository.findFirstByOrderByAmountSoldDesc();
        if (topProduct == null) {
            return List.of();
        }

        int maxAmountSold = topProduct.getAmountSold();

        return productRepository.findByAmountSold(maxAmountSold);
    }


    public void deleteProductById(String id) {
        UUID productUUID = UUID.fromString(id);
        productRepository.deleteById(productUUID);
    }



//    public Product updateProduct(UUID productId, Map<String, Object> updates) {
//        Product product = productRepository.findById(productId)
//                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
//
//        if (updates.containsKey("name")) {
//            product. = (String) updates.get("name");
//        }
//        if (updates.containsKey("price")) {
//            product.price = (Double) updates.get("price");
//        }
//        // Update other fields as needed...
//
//        return productRepository.save(product);
//    }
    public Optional<Product> updateProduct(String id, Map<String, Object> updates) {
        MongoDatabase mongoDatabase = this.mongoClient.getDatabase("elthon2yelamr7");
        MongoCollection<Document> products = mongoDatabase.getCollection("products");

        UUID productUUID = UUID.fromString(id);
        Optional<Product> productOptional = productRepository.findById(productUUID);

        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Product product = productOptional.get();

        if (updates.containsKey("name")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("name", (String) updates.get("name")));
        }

        if (updates.containsKey("price")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("price", ((Number) updates.get("price")).doubleValue()));
        }

        if (updates.containsKey("amountSold")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("amountSold", ((Number) updates.get("amountSold")).intValue()));
        }

        if (updates.containsKey("description")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("description", (String) updates.get("description")));
        }

        if (updates.containsKey("discount")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("discount", ((Number) updates.get("discount")).doubleValue()));
        }

        if (updates.containsKey("sellerId")) {
            products.updateOne(Filters.eq("_id", id),
                    Updates.set("sellerId", updates.get("sellerId").toString()));
        }

        // Subclass-specific fields
        if (product instanceof Food) {
            if (updates.containsKey("isVegetarian")) {
                products.updateOne(Filters.eq("_id", id),
                        Updates.set("isVegetarian", (Boolean) updates.get("isVegetarian")));
            }
            if (updates.containsKey("cuisineType")) {
                products.updateOne(Filters.eq("_id", id),
                        Updates.set("cuisineType", (String) updates.get("cuisineType")));
            }
        } else if (product instanceof Beverage) {
            if (updates.containsKey("isCarbonated")) {
                products.updateOne(Filters.eq("_id", id),
                        Updates.set("isCarbonated", (Boolean) updates.get("isCarbonated")));
            }
            if (updates.containsKey("isHot")) {
                products.updateOne(Filters.eq("_id", id),
                        Updates.set("isHot", (Boolean) updates.get("isHot")));
            }
        }

        return productRepository.findById(productUUID);
    }

    public Double applyDiscount(String id,Double discount){

        if (discount == null || discount < 0.0 || discount > 1.0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount must be between 0 and 1");
        }
        UUID productUUID = UUID.fromString(id);
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().set("discount", discount);
        mongoTemplate.updateFirst(query, update, Product.class);
        Optional<Product> productOptional = productRepository.findById(productUUID);

        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Product product = productOptional.get();
        return  product.getPrice() * (1-discount);

    }


    public Product incrementAmountSold(String id, int incrementBy) {
        if(incrementBy < 0) {
            throw new IllegalArgumentException("Increment value must be non-negative");
        }
        UUID productUUID = UUID.fromString(id);
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("amountSold", incrementBy);
        mongoTemplate.updateFirst(query, update, Product.class);
        return mongoTemplate.findOne(query, Product.class);

    }

    public Product decrementAmountSold(String id, int decrementBy) {

        if(decrementBy < 0) {
            throw new IllegalArgumentException("decrement value must be non-negative");
        }
        UUID productUUID = UUID.fromString(id);
        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("amountSold", -decrementBy);
        mongoTemplate.updateFirst(query, update, Product.class);
        return mongoTemplate.findOne(query, Product.class);
    }
}
