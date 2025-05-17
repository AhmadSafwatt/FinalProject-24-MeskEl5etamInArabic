package com.example.chatservice.utils;

import com.example.chatservice.dtos.ProductDTO;

public class ProductUtils {
    public static String prettyPrintProduct(ProductDTO product) {
        return String.format(
                "type = %s, id = %s, name = %s, sellerId = %s, price = %.2f, amountSold = %d, description = %s, discount = %.2f, cuisineType = %s, vegetarian = %b",
                product.getType(),
                product.getId(),
                product.getName(),
                product.getSellerId(),
                product.getPrice(),
                product.getAmountSold(),
                product.getDescription(),
                product.getDiscount(),
                product.getCuisineType(),
                product.isVegetarian()
        );
    }
}