package com.homechef.ProductService.model;

import java.util.UUID;

public class ProductFactory {

    public static Product createProduct(String type, String name, UUID sellerId, Double price) {
        switch (type.toLowerCase()) {
            case "beverage":
                return new Beverage.Builder()
                        .setName(name)
                        .setSellerId(sellerId)
                        .setPrice(price)
                        .build();
            case "snack":
                return new Food.Builder()
                        .setName(name)
                        .setSellerId(sellerId)
                        .setPrice(price)
                        .build();
            default:
                throw new IllegalArgumentException("Unknown product type: " + type);
        }
    }
}
