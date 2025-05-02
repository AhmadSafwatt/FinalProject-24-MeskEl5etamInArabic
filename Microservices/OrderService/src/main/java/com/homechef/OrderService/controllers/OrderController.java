package com.homechef.OrderService.controllers;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {this.orderService = orderService;}

    @GetMapping("/all")
    public List<Order> getAllOrders() {return orderService.getAllOrders();}
}
