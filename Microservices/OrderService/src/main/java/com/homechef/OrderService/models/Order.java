package com.homechef.OrderService.models;

import jakarta.persistence.*;

import java.util.List;
import java.util.UUID;

@Entity
public class Order {

    @Id
    private UUID id;
    private String buyerId;
    private String state;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<OrderItem> items;
}
