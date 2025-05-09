package com.homechef.ProductService.model;

import java.util.Map;
import java.util.UUID;

public class BeverageFactory extends ProductFactory {
    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold,String description,Double discount, Map<String, Object> request) {

        boolean isCarbonated = (boolean) request.get("isCarbonated");
        boolean isHot = (boolean) request.get("isHot");

        return new Beverage.Builder()
                .setName(name)
                .setSellerId(sellerId)
                .setPrice(price)
                .setAmountSold(amountSold)
                .setDiscount(discount)
                .setDescription(description)
                .setIsCarbonated(isCarbonated)
                .setIsHot(isHot)
                .build();
    }

}
