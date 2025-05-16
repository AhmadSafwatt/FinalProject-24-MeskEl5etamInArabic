package com.homechef.ProductService.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

public class FoodFactory extends ProductFactory {

    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold,String description, Double discount, Map<String, Object> request) {
        boolean isVegetarian = (boolean) request.get("isVegetarian");
        String cuisineType = (String) request.get("cuisineType");

        if (!request.containsKey("isVegetarian") || !request.containsKey("cuisineType") ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing required fields for Food");
        }

        return new Food.Builder()
                .setName(name)
                .setSellerId(sellerId)
                .setPrice(price)
                .setAmountSold(amountSold)
                .setDescription(description)
                .setDiscount(discount)
                .setIsVegetarian(isVegetarian)
                .setCuisineType(cuisineType)
                .build();
    }
}
