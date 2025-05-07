package com.homechef.OrderService.controllers;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

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

    // buyer is probably the one who will use this, to see all items in the order
    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable UUID orderId) {
        orderService.deleteOrder(orderId);
    }

    // update order state (also works for canceling an order)
    @PutMapping("/{orderId}/newState")
    public void updateOrderState(@PathVariable UUID orderId, @RequestBody String newState) {

        // check if new state is in one of the states written in the OrderStatus enum
        // -------------------------------------------
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newState.toUpperCase());
        } catch (IllegalArgumentException e) {
            // the illegal argument exception is thrown when the newState is not one of the
            // possible enum values (this is the behavior of the valueOf method in any enum)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid order state: " + newState + ". Valid values are: " +
                            String.join(", ", Arrays.stream(OrderStatus.values())
                                    .map(Enum::name)
                                    .toList()));
        }

        // check if the current state can logically be changed to the new state
        // -------------------------------------------
        try {
            orderService.updateOrderStatus(orderId, OrderStatus.getState(newStatus));
        } catch (IllegalArgumentException e) {
            // will be thrown by the "setOrderState" method / "cancelOrder" method
            // in the Order class if the operation is not allowed
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }
    }
}
