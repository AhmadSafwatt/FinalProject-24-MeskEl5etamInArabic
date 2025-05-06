package com.homechef.CartService.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CartBuilder {

//    private UUID id = UUID.randomUUID(); // default to random if not set
    private UUID customer_id;
    private List<CartItem> cartItems = new ArrayList<>();

    public CartBuilder customerId(UUID customer_id) {
        this.customer_id = customer_id;
        return this;
    }

//    public CartBuilder id(UUID id) {
//        this.id = id;
//        return this;
//    }

    public CartBuilder addCartItem(CartItem item) {
        this.cartItems.add(item);
        return this;
    }

    public CartBuilder cartItems(List<CartItem> items) {
        this.cartItems = items;
        return this;
    }

    public Cart build() {
        return new Cart( customer_id , cartItems);
    }
}

