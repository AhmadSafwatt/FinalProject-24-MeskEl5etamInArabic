package com.homechef.CartService.model;

public class CartMessage {
    Cart cart;
    Double totalPrice;

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    public Cart getCart() {
        return cart;
    }
    public Double getTotalPrice() {
        return totalPrice;
    }


    @Override
    public String toString() {
        return "CartMessage{" +
                "cart=" + cart +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
