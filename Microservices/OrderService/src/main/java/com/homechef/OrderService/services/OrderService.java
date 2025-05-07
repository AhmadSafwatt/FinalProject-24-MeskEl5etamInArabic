package com.homechef.OrderService.services;

import com.homechef.OrderService.clients.ProductServiceClient;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.repositories.OrderRepository;
import com.homechef.OrderService.states.CancelledState;
import com.homechef.OrderService.states.OrderState;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final ProductServiceClient productServiceClient;

    @Autowired
    public OrderService(OrderRepository orderRepository, EmailService emailService,
            ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.productServiceClient = productServiceClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) {
        Order createdOrder = orderRepository.save(order);
        sendOrderCreationNotifications(createdOrder);
        return createdOrder;
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
        decreaseProductSales(order);
        sendOrderDeletionNotifications(order);
    }

    public void updateOrderStatus(UUID orderId, OrderState newState) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        OrderState oldState = order.getState();
        if (newState instanceof CancelledState) {
            order.cancelOrder();
            orderRepository.save(order);
            decreaseProductSales(order);
            sendOrderCancellationNotification(order);
        } else {
            order.setOrderState(newState);
            orderRepository.save(order);
            sendOrderStatusUpdateNotification(order, oldState, newState);
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

    public void sendTestMail() {
        emailService.sendEmail("hussain.ghoraba@gmail.com", "Test Email",
                "This is a test email from Order Service");
        System.out.println("Email sent successfully");
    }

    // ------------------------------------------ helpers

    // TODO: waiting for Safwat team to implement the api
    private void decreaseProductSales(Order order) {
        for (OrderItem item : order.getItems()) {
            UUID productId = item.getProductId();
            int quantity = item.getQuantity();
            productServiceClient.modifyProductSales(productId, -quantity);
        }
    }

    // TODO: waiting for omar nour team to tell us how to get the mail by id
    private String getUserMailById(UUID id) {
        return "hussain.ghoraba@gmail.com";
    }

    // -------------------------------------------notifications
    private void notifyBuyer(Order order, String subject, String msgTail) {
        UUID buyerId = order.getBuyerId();
        String buyerEmail = getUserMailById(buyerId);
        String msg = "Your order with id " + order.getId() + " has been " + msgTail;
        emailService.sendEmail(buyerEmail, subject, msg);
    }

    private void notifySellers(Order order, String subject, String msgTail) {
        // Get unique sellers and their product ids
        HashMap<UUID, String> sellerProductIds = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            UUID sellerId = item.getSellerId();
            String productId = item.getProductId().toString();
            if (!sellerProductIds.containsKey(sellerId)) {
                sellerProductIds.put(sellerId, productId);
            } else {
                String existingProductIds = sellerProductIds.get(sellerId);
                sellerProductIds.put(sellerId, existingProductIds + ", " + productId);
            }
        }

        // send email to each seller
        for (UUID sellerId : sellerProductIds.keySet()) {
            String sellerEmail = getUserMailById(sellerId);
            String productIds = sellerProductIds.get(sellerId);
            String msg = "Your order with id " + order.getId() + " and product ids: "
                    + productIds + " has been " + msgTail;
            emailService.sendEmail(sellerEmail, subject, msg);
        }
    }

    private void sendOrderCreationNotifications(Order order) {
        notifyBuyer(order, "Order Creation", "created successfully");
        notifySellers(order, "To the kitchen!", "has been just created by a customer!");
    }

    private void sendOrderDeletionNotifications(Order order) {
        String msgTail = "deleted from our database, this is most likely done by our customer service for some reason or another !.";
        notifyBuyer(order, "Order Deletion", msgTail);
        notifySellers(order, "Order Deletion", msgTail);
    }

    private void sendOrderCancellationNotification(Order order) {
        notifyBuyer(order, "Order Cancellation", "cancelled");
        notifySellers(order, "Order Cancellation", "cancelled");
    }

    private void sendOrderStatusUpdateNotification(Order order, OrderState oldState, OrderState newState) {
        notifyBuyer(order, "Order Status Update", "updated from " + oldState.getOrderStatus() + " to "
                + newState.getOrderStatus());
    }

}