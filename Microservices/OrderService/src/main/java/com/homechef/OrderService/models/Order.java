package com.homechef.OrderService.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.homechef.OrderService.DTOs.CartDTO;
import com.homechef.OrderService.states.*;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonProperty
    private UUID id;

    private UUID buyerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;

    private Double totalPrice = 0.0;

    private String orderNote = "";

    @Transient
    @JsonIgnore
    private OrderState state = new CreatedState();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @JsonCreator
    public Order(
            @JsonProperty("buyerId") UUID buyerId,
            @JsonProperty("items") List<OrderItem> items,
            @JsonProperty("totalPrice") Double totalPrice,
            @JsonProperty("orderNote") String orderNote) {
        this.buyerId = buyerId;
        this.status = OrderStatus.CREATED;
        this.state = new CreatedState();
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.totalPrice = totalPrice;
        this.orderNote = orderNote;
        items.forEach(item -> item.setOrder(this));
    }

    // setter override
    public void setStatus(OrderStatus status) {
        this.status = status;
        initState();
    }

    public void setState(OrderState state) {
        this.state = state;
        setStatus(state.getOrderStatus());
    }

    @PostLoad
    public void initState() {
        this.state = OrderStatus.getState(this.status);
    }

    public void setOrderState(OrderState state) {
        this.state.setOrderState(this, state);
    }

    public void cancelOrder() {
        this.state.cancelOrder(this);
    }

    public void updateItemNote(UUID productId, String note) {
        this.state.updateItemNote(this, productId, note);
    }

    // setter override
    public void setItems(List<OrderItem> items) {
        this.items = items;
        for (OrderItem item : items) {
            item.setOrder(this);
        }
    }

    public void addItem(OrderItem item) {
        if (items == null) {
            items = new java.util.ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    public CartDTO toCartDTO() {
        return new CartDTO(
                this.getBuyerId(),
                this.getItems().stream().map(OrderItem::toCartItemDTO)
                        .toList(),
                this.getOrderNote(),
                false   // The promo must be re-initialized at the cart level
        );
    }
}
