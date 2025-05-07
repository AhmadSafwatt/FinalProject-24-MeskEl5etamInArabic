package com.homechef.ProductService.model;

import java.util.UUID;

public class FoodFactory extends ProductFactory {

    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold) {
        return new Food.Builder()
                .setName(name)
                .setSellerId(sellerId)
                .setPrice(price)
                .setAmountSold(amountSold)
                .build();
    }
}
