package com.homechef.OrderService.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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
        return new Order(
                this.getCustomerId(),
                this.getCartItems().stream().map(CartItemDTO::toOrderItem)
                        .collect(Collectors.toList()),
                totalPrice,
                this.getNotes()
        );
    }

}
