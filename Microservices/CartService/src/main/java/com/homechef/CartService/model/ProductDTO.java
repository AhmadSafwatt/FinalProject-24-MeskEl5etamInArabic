package com.homechef.CartService.model;

import java.util.UUID;

public class ProductDTO {

    UUID id;
    String name;
    UUID sellerId;
    Double price;
    int amountSold;
    String description;
    Double discount;
    private Boolean isVegetarian;
    private String cuisineType;
    private Boolean isCarbonated;
    private Boolean isHot;
   
    public ProductDTO() {
    }


    public ProductDTO(UUID id, String name, UUID sellerId, Double price, int amountSold, String description, Double discount, String cuisineType, boolean isVegetarian) {
        this.id = id;
        this.name = name;
        this.sellerId = sellerId;
        this.price = price;
        this.amountSold = amountSold;
        this.description = description;
        this.discount = discount;
        this.isVegetarian = isVegetarian;
        this.cuisineType = cuisineType;
        this.isCarbonated = null;
        this.isHot = null;
    }
    public ProductDTO(UUID id, String name, UUID sellerId, Double price, int amountSold, String description, Double discount, boolean isHot, boolean isCarbonated) {
        this.id = id;
        this.name = name;
        this.sellerId = sellerId;
        this.price = price;
        this.amountSold = amountSold;
        this.description = description;
        this.discount = discount;
        this.isHot = isHot;
        this.isCarbonated = isCarbonated;
        this.cuisineType = null;
        this.isVegetarian = null;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getAmountSold() {
        return amountSold;
    }

    public void setAmountSold(int amountSold) {
        this.amountSold = amountSold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Boolean isCarbonated() {
        return isCarbonated;
    }

    public void setCarbonated(boolean carbonated) {
        isCarbonated = carbonated;
    }

    public Boolean isHot() {
        return isHot;
    }

    public void setHot(boolean hot) {
        isHot = hot;
    }

    public Boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }
}