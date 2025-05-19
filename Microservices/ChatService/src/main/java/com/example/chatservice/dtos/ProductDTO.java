package com.example.chatservice.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductDTO {

    private String type;
    private UUID id;
    private String name;
    private UUID sellerId;
    private double price;
    private int amountSold;
    private String description;
    private double discount;
    private String cuisineType;
    private boolean vegetarian;
}