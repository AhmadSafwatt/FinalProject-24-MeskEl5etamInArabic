package com.homechef.ProductService.model;

import java.util.Map;
import java.util.UUID;

public class FoodFactory extends ProductFactory {

    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold,String description, Double discount, Map<String, Object> request) {
        boolean isVegetarian = (boolean) request.get("isVegetarian");
        String cuisineType = (String) request.get("cuisineType");

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
