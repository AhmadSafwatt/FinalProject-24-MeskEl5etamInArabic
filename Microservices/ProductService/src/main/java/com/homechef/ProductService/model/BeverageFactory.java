package com.homechef.ProductService.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

public class BeverageFactory extends ProductFactory {
    @Override
    public Product createProduct(String name, UUID sellerId, Double price, int amountSold,String description,Double discount, Map<String, Object> request) {

        if (!request.containsKey("isCarbonated") || !request.containsKey("isHot") ){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Missing required fields for Beverage");
        }
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
