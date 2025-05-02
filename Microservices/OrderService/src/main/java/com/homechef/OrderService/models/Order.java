package com.homechef.OrderService.models;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    private UUID id;
    private String buyerId;
    private String state;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public Order() {}
    public Order(UUID id, String buyerId, String state, List<OrderItem> items) {
        this.id = id;
        this.buyerId = buyerId;
        this.state = state;
        this.items = items;
    }
    public Order(String buyerId, String state, List<OrderItem> items) {
        this.id = UUID.randomUUID();
        this.buyerId = buyerId;
        this.state = state;
        this.items = items;
    }
    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}
    public String getBuyerId() {return buyerId;}
    public void setBuyerId(String buyerId) {this.buyerId = buyerId;}
    public String getState() {return state;}
    public void setState(String state) {this.state = state;}
    public List<OrderItem> getItems() {return items;}
    public void setItems(List<OrderItem> items) {this.items = items;}
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }
}
