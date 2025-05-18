package com.homechef.OrderService.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
public class CartDTO {

    @JsonProperty("id")
    UUID id;
    UUID customerId;
    @JsonProperty("cartItems")
    List<CartItemDTO> cartItems;
    String notes;
    boolean promo;

    public CartDTO() {
        this.id = UUID.randomUUID();
    }

    public CartDTO(UUID customerId, List<CartItemDTO> cartItems, String notes, boolean promo) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.cartItems = cartItems;
        this.notes = notes;
        this.promo = promo;
    }

    public CartDTO(UUID id, UUID customerId, List<CartItemDTO> cartItems, String notes, boolean promo) {
        this.id = id;
        this.customerId = customerId;
        this.cartItems = cartItems;
        this.notes = notes;
        this.promo = promo;
    }

    public Order toOrder(Double totalPrice) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItemDTO cartItem : this.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setSellerId(cartItem.getSellerId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setNotes(cartItem.getNotes());

            orderItems.add(orderItem);
        }
        return new Order(
                this.getCustomerId(),
                orderItems,
                totalPrice,
                this.getNotes()
        );
    }

}
