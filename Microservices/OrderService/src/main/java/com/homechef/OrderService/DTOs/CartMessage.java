package com.homechef.OrderService.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CartMessage {
    @JsonProperty("cart")
    CartDTO cartDTO;
    Double totalPrice;

    public CartMessage() {}
    public CartMessage(CartDTO cart, double totalPrice) {
        this.cartDTO = cart;
        this.totalPrice = totalPrice;
    }
}