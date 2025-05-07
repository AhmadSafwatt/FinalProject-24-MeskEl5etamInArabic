package com.homechef.ProductService.model;

import java.util.UUID;

public class BeverageFactory extends ProductFactory {
    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold,String description,Double discount) {
        return new Beverage.Builder()
                .setName(name)
                .setSellerId(sellerId)
                .setPrice(price)
                .setAmountSold(amountSold)
                .setDiscount(discount)
                .setDescription(description)
                .build();
    }

}
