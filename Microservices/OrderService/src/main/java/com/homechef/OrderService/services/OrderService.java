package com.homechef.OrderService.services;

import com.homechef.OrderService.DTOs.CartDTO;
import com.homechef.OrderService.DTOs.CartMessage;
import com.homechef.OrderService.clients.ProductServiceClient;
import com.homechef.OrderService.clients.AuthServiceClient;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.rabbitmq.OrderRabbitMQConfig;
import com.homechef.OrderService.rabbitmq.RabbitMQProducer;
import com.homechef.OrderService.repositories.OrderRepository;
import com.homechef.OrderService.states.CancelledState;
import com.homechef.OrderService.states.OrderState;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final EmailService emailService;
    private final ProductServiceClient productServiceClient;
    private final RabbitMQProducer rabbitMQProducer;
    private final AuthServiceClient authServiceClient;

    @Autowired
    public OrderService(OrderRepository orderRepository, EmailService emailService,
            RabbitMQProducer rabbitMQProducer, AuthServiceClient authServiceClient, ProductServiceClient productServiceClient) {
        this.orderRepository = orderRepository;
        this.emailService = emailService;
        this.rabbitMQProducer = rabbitMQProducer;
        this.authServiceClient = authServiceClient;
        this.productServiceClient = productServiceClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order createOrder(Order order) { return createOrder(order, order.getBuyerId()); }
    public Order createOrder(Order order, UUID buyerId) { // added buyerId parameter for controller authorization
        order.setBuyerId(buyerId);
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

    public void updateOrderStatus(UUID orderId, OrderState newState) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        OrderState oldState = order.getState();
        if (newState instanceof CancelledState) {
            order.cancelOrder();
            orderRepository.save(order);
            // TODO: uncomment this line when the api is ready
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            // will be thrown by the updateItemNote method if no product with the given id
            // found in the given order, or if the state can't of the order
            // does not allow note editing
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }
        orderRepository.save(order);

        // no need to notify anyone, this is just a note
    }

    // ------------------------------------------ helpers

    // TODO: should be Async ? DONE
    private void decreaseProductSales(Order order) {
        for (OrderItem item : order.getItems()) {
            UUID productId = item.getProductId();
            int quantity = item.getQuantity();
            productServiceClient.decrementAmountSold(productId.toString(), quantity); //SYNC communication
            // new ASYNC communication
            //rabbitMQProducer.sendProductDecrement(productId, quantity);
            // Swapped back to sync, but both are working if changes are made
        }
    }

    private String getUserMailById(UUID id) {
        // the api called in the feign client takes a list of ids,
        // and returns a map of ids to emails
        Map<String, String> emails = authServiceClient.getUsersEmails(List.of(id));
        if (emails.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "fetched mail of user with id " + id + " is empty");
        }
        return emails.get(id.toString());
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
        HashMap<UUID, List<String>> sellerProductIds = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            UUID sellerId = item.getSellerId();
            String productId = item.getProductId().toString();
            if (!sellerProductIds.containsKey(sellerId)) {
                sellerProductIds.put(sellerId, List.of(productId));
            } else {
                sellerProductIds.get(sellerId).add(productId);
            }
        }

        // send email to each seller
        for (UUID sellerId : sellerProductIds.keySet()) {
            String sellerEmail = getUserMailById(sellerId);
            String productIds = sellerProductIds.get(sellerId).toString();
            String msg = "Your order with id " + order.getId() + " and product ids: "
                    + productIds + " has been " + msgTail;
            emailService.sendEmail(sellerEmail, subject, msg);
        }
    }

    private void sendOrderCreationNotifications(Order order) {
        notifyBuyer(order, "Order Creation", "created successfully");
        notifySellers(order, "To the kitchen!", "has been just created by a customer!");
    }

    private void sendOrderCancellationNotification(Order order) {
        notifyBuyer(order, "Order Cancellation", "cancelled");
        notifySellers(order, "Order Cancellation", "cancelled");
    }

    private void sendOrderStatusUpdateNotification(Order order, OrderState oldState, OrderState newState) {
        notifyBuyer(order, "Order Status Update", "updated from " + oldState.getOrderStatus() + " to "
                + newState.getOrderStatus());
    }

    @RabbitListener(queues = OrderRabbitMQConfig.CART_QUEUE)
    public void receiveCartMessage(@Payload CartMessage cartMessage) {
        System.out.println("Received cart message: " + cartMessage);
        CartDTO cart = cartMessage.getCartDTO();
        Double price = cartMessage.getTotalPrice();
        System.out.println("Received cart: " + cart);
        System.out.println("Received total price: " + price);

        // Process the received cart
        createOrder(cart.toOrder(price));
    }

    public void reOrderAndSendItemsToCart(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order with id " + orderId + " not found"));
        CartDTO cart = order.toCartDTO();
        cart.setId(null); // set id to null to create a new cart

        // Send the cart to the cart service
        CartMessage cartMessage = new CartMessage(cart, 0.0);
        rabbitMQProducer.sendCartReOrderMessage(cartMessage);
    }

}