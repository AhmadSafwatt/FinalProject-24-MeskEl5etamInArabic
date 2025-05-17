package com.homechef.CartService.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homechef.CartService.model.Cart;

public class CartMessage {
    Cart cart;
    Double totalPrice;

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
}