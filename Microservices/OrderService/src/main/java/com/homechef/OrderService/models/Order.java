package com.homechef.OrderService.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    private UUID buyerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime orderDate;
    private Double totalPrice = 0.0;

    @Transient
    @JsonIgnore
    private OrderState state = new CreatedState();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;


    @JsonCreator
    public Order(
        @JsonProperty("buyerId") UUID buyerId,
        @JsonProperty("items") List<OrderItem> items,
        @JsonProperty("totalPrice") Double totalPrice
    ) {
        this.buyerId = buyerId;
        this.status = OrderStatus.CREATED;
        this.state = new CreatedState();
        this.items = items;
        this.orderDate = LocalDateTime.now();
        this.totalPrice = totalPrice;
        items.forEach(item -> item.setOrder(this));
    }

    public Order(UUID buyerId, OrderStatus status, List<OrderItem> items, Double totalPrice) {
        this(buyerId, items, totalPrice);
        setStatus(status);
    }

    // setter override
    public void setStatus(OrderStatus status) {this.status = status; initState();}
    public void setState(OrderState state) {this.state = state; setStatus(state.getOrderStatus());}

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
}
