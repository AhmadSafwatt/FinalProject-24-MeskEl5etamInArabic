package com.homechef.OrderService.services;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.repositories.OrderRepository;
import com.homechef.OrderService.states.CancelledState;
import com.homechef.OrderService.states.OrderState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    @Autowired
    public OrderService(OrderRepository orderRepository, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        return orderRepository.save(order);
        // TODO: notify buyer & seller
    }

    public List<Order> getAllOrdersByBuyerId(UUID buyerId) {
        return orderRepository.findAllByBuyerId(buyerId);
    }

    public List<Order> getFilteredOrdersBySellerId(UUID sellerId) {
        List<Order> sellerOrders = orderRepository.findAllOrdersContainingSellerId(sellerId);
        sellerOrders.forEach(order -> order.setItems(
                order.getItems().stream()
                        .filter(item -> item.getSellerId().equals(sellerId))
                        .toList()));
        return sellerOrders;
    }

    public Order getOrderByIdFilteredBySellerId(UUID orderId, UUID sellerId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            // order not found
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found");
        }
        order.setItems(
                order.getItems().stream()
                        .filter(item -> item.getSellerId().equals(sellerId))
                        .toList());
        if (order.getItems().isEmpty()) {
            // not allowed to access this order
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Order does not belong to this seller");
        }
        return order;
    }

    // for buyer use
    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
    }

    public void deleteOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        orderRepository.delete(order);
        // no need to notify anyone, because this method
        // will not be used anyway in our system,
        // we did not determine when would the order be deleted
    }

    public void updateOrderStatus(UUID orderId, OrderState newState) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        OrderState oldState = order.getState();
        if (newState instanceof CancelledState) {
            order.cancelOrder();
            orderRepository.save(order);
            updateProductSales(orderId);
            // TODO: notify buyer & seller
        } else {
            order.setOrderState(newState);
            orderRepository.save(order);
            // TODO: notify buyer

        }
    }

    public void updateItemNote(UUID orderId, UUID productId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        try {
            order.updateItemNote(productId, note);
        } catch (IllegalArgumentException e) {
            // will be thrown by the updateItemNote method if no product with the given id
            // found in the given order
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }
        orderRepository.save(order);

        // no need to notify anyone, this is just a note
    }

    private void updateProductSales(UUID orderId) {
        // TODO: send api request to decrease the product sales, waiting for
        // Safwat team to implement the api
    }

    public void testMail() {
        emailService.sendEmail("hussain.ghoraba@gmail.com", "Test Email",
                "This is a test email from Order Service");
        System.out.println("Email sent successfully");
    }

}
