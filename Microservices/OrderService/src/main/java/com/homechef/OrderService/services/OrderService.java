package com.homechef.OrderService.services;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {this.orderRepository = orderRepository;}

    public List<Order> getAllOrders() { return orderRepository.findAll(); }

    public Order createOrder(Order order) {return orderRepository.save(order);}

    public List<Order> getAllOrdersByBuyerId(UUID buyerId) {
        return orderRepository.findAllByBuyerId(buyerId);
    }

    public List<Order> getFilteredOrdersBySellerId(UUID sellerId) {
        List<Order> sellerOrders =  orderRepository.findAllOrdersContainingSellerId(sellerId);
        sellerOrders.forEach(order -> order.setItems(
                order.getItems().stream()
                        .filter(item -> item.getSellerId().equals(sellerId))
                        .toList()
        ));
        return sellerOrders;
    }

    public Order getOrderByIdFilteredBySellerId(UUID orderId, UUID sellerId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // order not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Order not found");
        }
        order.setItems(
                order.getItems().stream()
                        .filter(item -> item.getSellerId().equals(sellerId))
                        .toList()
        );
        if (order.getItems().isEmpty()) {
            // not allowed to access this order
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Order does not belong to this seller");
        }
        return order;
    }
}
