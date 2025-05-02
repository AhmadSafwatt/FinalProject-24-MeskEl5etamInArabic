package com.homechef.CartService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

//Cart:
//Customer ID
//List of cartIems
//
//CartItem:
//ProductID
//        Quantity
//DateAdded
//        Notes

@Document(collection = "carts")
public class Cart {

    @Id
    UUID id;
    UUID customerId;
    List<CartItem> cartItems;

    public Cart() {
    }

    public Cart(UUID customer_id, List<CartItem> cartItems) {
        this.customerId = customer_id;
        this.cartItems = cartItems;
    }

    public Cart(UUID id, UUID customer_id, List<CartItem> cartItems) {
        this.id = id;
        this.customerId = customer_id;
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
