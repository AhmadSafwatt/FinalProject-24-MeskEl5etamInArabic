package com.homechef.OrderService.services;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {this.orderRepository = orderRepository;}

    public List<Order> getAllOrders() { return orderRepository.findAll(); }

    public Order createOrder(Order order) { return orderRepository.save(order); }

    public List<Order> getAllOrdersByBuyerId(UUID buyerId) {
        return orderRepository.findAllByBuyerId(buyerId);
    }
}
