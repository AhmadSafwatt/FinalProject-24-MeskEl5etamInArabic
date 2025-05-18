package com.homechef.OrderService.DTOs;

import com.homechef.OrderService.models.OrderItem;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CartItemDTO {

    UUID productId;
    int quantity;
    LocalDateTime dateAdded;
    String notes = "";
    UUID sellerId;
    private ProductDTO product = null;


    public CartItemDTO() {}

    public CartItemDTO(UUID productId, int quantity, LocalDateTime dateAdded, UUID sellerId, String notes) {
        this.productId = productId;
        this.quantity = quantity;
        this.dateAdded = dateAdded;
        this.sellerId = sellerId;
        this.notes = notes;
    }

    public OrderItem toOrderItem() {
        return new OrderItem(null, productId, sellerId, quantity, notes);
    }
}
