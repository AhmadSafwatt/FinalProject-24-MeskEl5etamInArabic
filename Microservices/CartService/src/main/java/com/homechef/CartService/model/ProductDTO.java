package com.homechef.CartService.model;

import java.util.UUID;

public class ProductDTO {

    UUID id;
    String name;
    UUID sellerId;
    Double price;
    int amountSold;
   
    public ProductDTO() {
    }
    
    public ProductDTO(UUID id, String name, UUID sellerId, Double price, int amountSold) {
        this.id = id;
        this.name = name;
        this.sellerId = sellerId;
        this.price = price;
        this.amountSold = amountSold;
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

    

}