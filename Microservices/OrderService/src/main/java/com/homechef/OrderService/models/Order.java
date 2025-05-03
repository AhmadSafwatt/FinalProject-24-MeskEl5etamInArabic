package com.homechef.OrderService.models;

import com.homechef.OrderService.states.*;
import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;
    private UUID buyerId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Transient
    private OrderState state = new CreatedState();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public Order() {}
    public Order(UUID id, UUID buyerId, OrderStatus status, List<OrderItem> items) {
        this.id = id;
        this.buyerId = buyerId;
        this.status = status;
        this.items = items;
    }
    public Order(UUID buyerId, OrderStatus status, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.buyerId = buyerId;
        this.status = status;
        this.items = items;
    }
    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}
    public UUID getBuyerId() {return buyerId;}
    public void setBuyerId(UUID buyerId) {this.buyerId = buyerId;}
    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status; initState();}
    public OrderState getState() {return state;}
    public void setState(OrderState state) {this.state = state; setStatus(OrderState.getOrderStatus(state));}
    public List<OrderItem> getItems() {return items;}
    public void setItems(List<OrderItem> items) {this.items = items;}
    public void addItem(OrderItem item) {this.items.add(item);}

    public void setOrderState(OrderState state) {
        this.state.setOrderState(this, state);
    }
    public void cancelOrder() {
        this.state.cancelOrder(this);
    }
    public void updateItemNote(UUID productId, String note) {
        this.state.updateItemNote(this, productId, note);
    }

    @PostLoad
    public void initState() {
        this.state = OrderStatus.getState(this.status);
    }
}
