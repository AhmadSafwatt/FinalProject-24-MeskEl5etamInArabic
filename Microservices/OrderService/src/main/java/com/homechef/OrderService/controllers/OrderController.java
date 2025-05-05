package com.homechef.OrderService.controllers;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {this.orderService = orderService;}

    @GetMapping
    public List<Order> getAllOrders() {return orderService.getAllOrders();}

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        return orderService.createOrder(order);
    }

    @GetMapping("/buyer/{buyerId}")
    public List<Order> getAllOrdersByBuyerId(@PathVariable String buyerId) {
        return orderService.getAllOrdersByBuyerId(UUID.fromString(buyerId));
    }

    @GetMapping("/seller/{sellerId}")
    public List<Order> getFilteredOrdersBySellerId(@PathVariable String sellerId) {
        return orderService.getFilteredOrdersBySellerId(UUID.fromString(sellerId));
    }

    @GetMapping("/{orderId}/seller/{sellerId}")
    public Order getOrderByIdFilteredBySellerId(@PathVariable String orderId, @PathVariable String sellerId) {
            return orderService.getOrderByIdFilteredBySellerId(UUID.fromString(orderId), UUID.fromString(sellerId));
    }
}
