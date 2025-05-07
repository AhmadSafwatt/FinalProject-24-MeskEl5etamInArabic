package com.homechef.CartService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;

public class CartItem {

    @Field(targetType = FieldType.STRING)
    UUID productId;
    int quantity;
    LocalDateTime dateAdded;
    String notes;
    @Field(targetType = FieldType.STRING)
    UUID sellerId;


    public CartItem() {
    }

    public CartItem(UUID productId, int quantity, LocalDateTime dateAdded, String notes , UUID sellerId) {
        this.productId = productId;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.notes = notes;
        this.sellerId = sellerId;
    }

   

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getSellerId() {
        return sellerId;
    }

    public void setSellerId(UUID sellerId) {
        this.sellerId = sellerId;
    }

   
}
