package com.homechef.ProductService.service;


import com.homechef.ProductService.DTO.ProductMessage;
import com.homechef.ProductService.model.*;
import com.homechef.ProductService.rabbitmq.RabbitMQConfig;
import com.homechef.ProductService.repository.ProductRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.awt.*;
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


        if (!request.containsKey("type") || !request.containsKey("name") || !request.containsKey("sellerId") ||
                !request.containsKey("price")|| !request.containsKey("amountSold")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing required fields for Product");
        }






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

        if (!productRepository.existsById(productUUID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return productRepository.findById(productUUID).orElse(null);
    }

    public List<Product> getProductsById(List<String> ids) {
        List<Product> products = new ArrayList<>();
        for (String id : ids){
            UUID productUUID = UUID.fromString(id);
            if (!productRepository.existsById(productUUID)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
            }
            Product product = productRepository.findById(productUUID).orElse(null);
            products.add(product);
        }
        return products;
    }



    public List<Product> getMostSoldProducts() {
        Product topProduct = productRepository.findFirstByOrderByAmountSoldDesc();
        if (topProduct == null) {
            return List.of();
        }

        int maxAmountSold = topProduct.getAmountSold();

        return productRepository.findByAmountSold(maxAmountSold);
    }


    public void deleteProductById(String id,UUID sellerId) {
        UUID productUUID = UUID.fromString(id);


        if(!productRepository.existsById(productUUID)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        UUID productSellerID = productRepository.findById(productUUID).get().getSellerId();

        if(!productSellerID.equals(sellerId)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized to access this cart");
        }



        productRepository.deleteById(productUUID);
    }




    public Optional<Product> updateProduct(String id, Map<String, Object> updates,UUID sellerId) {
        MongoDatabase mongoDatabase = this.mongoClient.getDatabase("elthon2yelamr7");
        MongoCollection<Document> products = mongoDatabase.getCollection("products");

        UUID productUUID = UUID.fromString(id);
        Optional<Product> productOptional = productRepository.findById(productUUID);

        if (productOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        Product product = productOptional.get();

        if(!product.getSellerId().equals(sellerId)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"user not authorised to update this product");
        }

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

    public Double applyDiscount(String id,Double discount,UUID sellerId){

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
        if(!product.getSellerId().equals(sellerId)){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"user not authorised to apply discount for this product");
        }

        return  product.getPrice() * (1-discount);

    }

    @RabbitListener(queues = RabbitMQConfig.INCREMENT_QUEUE)
    public void incrementAmountSold(ProductMessage message) {
        UUID productUUID = message.getProductId();
        if(!productRepository.existsById(productUUID))
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }

        if(message.getAmount() < 0 ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Increment value must be non-negative");
        }

        Query query = new Query(Criteria.where("_id").is(message.getProductId().toString()));
        Update update = new Update().inc("amountSold", message.getAmount());
        mongoTemplate.updateFirst(query, update, Product.class);
    }
    //@RabbitListener(queues = RabbitMQConfig.DECREMENT_QUEUE)
    public void decrementAmountSold(String id, int amount) {
        //UUID productUUID = message.getProductId();
        UUID productUUID = UUID.fromString(id);
        if(!productRepository.existsById(productUUID))
        {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        int productAmountSold = productRepository.findById(productUUID).get().getAmountSold();
        if( productAmountSold< amount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"decrement value must be less than amount sold");
        }

        if(amount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"decrement value must be non-negative");
        }

        Query query = new Query(Criteria.where("_id").is(id));
        Update update = new Update().inc("amountSold",-1*amount);
        mongoTemplate.updateFirst(query, update, Product.class);
    }
}
