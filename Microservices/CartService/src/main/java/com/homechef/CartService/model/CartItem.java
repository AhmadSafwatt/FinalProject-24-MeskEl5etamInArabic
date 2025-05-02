package com.homechef.CartService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.UUID;

public class CartItem {

    @Field(targetType = FieldType.STRING)
    UUID product_id;
    int quantity;
    LocalDateTime dateAdded;
    String notes;
    @Field(targetType = FieldType.STRING)
    UUID seller_id;


    public CartItem() {
    }

    public CartItem(UUID product_id, int quantity, LocalDateTime dateAdded, UUID seller_id, String notes) {
        this.product_id = product_id;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.seller_id = seller_id;
        this.notes = notes;
    }

    public UUID getProduct_id() {
        return product_id;
    }

    public void setProduct_id(UUID product_id) {
        this.product_id = product_id;
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

    public UUID getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(UUID seller_id) {
        this.seller_id = seller_id;
    }
}
