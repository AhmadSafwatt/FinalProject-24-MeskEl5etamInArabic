package com.homechef.ProductService.seed;

import com.homechef.ProductService.model.Beverage;
import com.homechef.ProductService.model.Food;
import com.homechef.ProductService.repository.ProductRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class DatabaseSeeder {

    private final ProductRepository productRepository;

    public DatabaseSeeder(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void seed() {
        productRepository.deleteAll();

        // Seed beverages
        Beverage beverage1 = new Beverage.Builder()
                .setName("Cola")
                .setId(UUID.fromString("16b9eb22-301d-4ee9-bb21-88d9f52d08e0"))
                .setSellerId(UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13"))
                .setPrice(20.0)
                .setAmountSold(80)
                .setDescription("Refreshing cola drink")
                .setDiscount(0.0)
                .setIsCarbonated(true)
                .setIsHot(false)
                .build();

        Beverage beverage2 = new Beverage.Builder()
                .setName("Hot Chocolate")
                .setId(UUID.fromString("0248f6fe-33a9-4585-b23f-e7ddf7aab32f"))
                .setSellerId(UUID.fromString("e24f4ca0-d6d5-4a91-b453-45dde29067d5"))
                .setPrice(50.0)
                .setAmountSold(60)
                .setDescription("Delicious hot chocolate")
                .setDiscount(0.5)
                .setIsCarbonated(false)
                .setIsHot(true)
                .build();

        // Seed food
        Food food1 = new Food.Builder()
                .setName("Pizza")
                .setId(UUID.fromString("da85f125-3a17-4eab-a28f-a8ec2c9e18d8"))
                .setSellerId(UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13"))
                .setPrice(100.0)
                .setAmountSold(133)
                .setDescription("Cheesy pizza")
                .setDiscount(1.0)
                .setIsVegetarian(false)
                .setCuisineType("Italian")
                .build();

        Food food2 = new Food.Builder()
                .setName("Falafel")
                .setId(UUID.fromString("80b99e96-4bf6-44bc-a691-10c3212a7ffb"))
                .setSellerId(UUID.fromString("e24f4ca0-d6d5-4a91-b453-45dde29067d5"))
                .setPrice(10.0)
                .setAmountSold(0)
                .setDescription("Vegetarian falafel wrap")
                .setDiscount(0.05)
                .setIsVegetarian(true)
                .setCuisineType("Middle Eastern")
                .build();

        productRepository.saveAll(List.of(beverage1, beverage2, food1, food2));
    }
}