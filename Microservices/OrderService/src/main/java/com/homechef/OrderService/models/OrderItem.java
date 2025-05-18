package com.homechef.OrderService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.homechef.OrderService.DTOs.CartItemDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "order_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "order_id", "product_id" })
})
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnore
    private Order order;

    private UUID productId;

    private UUID sellerId;
    private Integer quantity;
    private String notes = "";

    public OrderItem(Order order, UUID productId, UUID sellerId, Integer quantity, String notes) {
        this.order = order;
        this.productId = productId;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.notes = notes;
    }

    public CartItemDTO toCartItemDTO() {
        return new CartItemDTO(
                this.productId,
                this.quantity,
                LocalDateTime.now(),
                this.sellerId,
                this.notes
        );
    }

}
