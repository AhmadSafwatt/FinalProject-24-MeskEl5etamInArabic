package com.homechef.CartService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;
import java.util.UUID;

@Document(collection = "carts")
public class Cart {

    @Id
    @Field(targetType = FieldType.STRING)   // force mongo to store the UUID as a string
    UUID id;
    @Field(targetType = FieldType.STRING)
    UUID customerId;
    List<CartItem> cartItems;

    public Cart() {
        this.id = UUID.randomUUID();
    }

    public Cart(UUID customerId, List<CartItem> cartItems) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.cartItems = cartItems;
    }

    public Cart(UUID id, UUID customerId, List<CartItem> cartItems) {
        this.id = id;
        this.customerId = customerId;
        this.cartItems = cartItems;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }
}
