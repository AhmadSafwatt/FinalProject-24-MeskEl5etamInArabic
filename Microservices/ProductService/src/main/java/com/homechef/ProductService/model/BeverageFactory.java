package com.homechef.ProductService.model;

import java.util.UUID;

public class BeverageFactory extends ProductFactory {
    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold) {
        return new Beverage.Builder()
                .setName(name)
                .setSellerId(sellerId)
                .setPrice(price)
                .setAmountSold(amountSold)
                .build();
    }

}
