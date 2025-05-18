package com.homechef.OrderService.controllers;

import com.homechef.OrderService.config.JwtUtil;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.services.OrderService;
import com.homechef.OrderService.services.OrdersSeeder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrdersSeeder ordersSeeder;
    private JwtUtil jwtUtil = JwtUtil.getInstance();

    @Autowired
    public OrderController(OrderService orderService, OrdersSeeder ordersSeeder) {
        this.orderService = orderService;
        this.ordersSeeder = ordersSeeder;
    }

    @GetMapping("/seed")
    public String seedOrders() {
        ordersSeeder.seed();
        return "Orders seeded successfully";
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public Order createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody Order order) {
        UUID buyerId = UUID.fromString(getUserField(authHeader, "id"));
        return orderService.createOrder(order, buyerId);
    }

    @GetMapping("/buyer/{buyerId}")
    public List<Order> getAllOrdersByBuyerId(@RequestHeader("Authorization") String authHeader) {
        String buyerId = getUserField(authHeader, "id");
        return orderService.getAllOrdersByBuyerId(UUID.fromString(buyerId));
    }

    @GetMapping("/seller")
    public List<Order> getFilteredOrdersBySellerId(@RequestHeader("Authorization") String authHeader) {
        String sellerId = getUserField(authHeader, "id");
        return orderService.getFilteredOrdersBySellerId(UUID.fromString(sellerId));
    }

    @GetMapping("/{orderId}/seller")
    public Order getOrderByIdFilteredBySellerId(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        String sellerId = getUserField(authHeader, "id");
        return orderService.getOrderByIdFilteredBySellerId(UUID.fromString(orderId), UUID.fromString(sellerId));
    }

    // buyer is probably the one who will use this, to see all items in the order
    @GetMapping("/{orderId}")
    public Order getOrderById(@RequestHeader("Authorization") String authHeader, @PathVariable UUID orderId) {
        validateeUserIsOrderBuyer(authHeader, orderId);
        return orderService.getOrderById(orderId);
    }

    @PutMapping("/{orderId}/newState")
    // --------------------------- note 1 :
    // update state (you can also set new state = CANCELLED to cancel the order)
    // and extra procedures for cancellation will be handled by the order
    // service automatically

    // --------------------------- note 2 :
    // in a realistic scenario, the order seller should only change the state
    // of the order item, and upon every order seller action, we should check if all
    // items in the order in the same state, if yes, then we should change the
    // state of the order automatically, not via api ! so ideally, there should be
    // an api that changes the state of the order item only, not the whole order.
    // that is why currently, we didn't add authorization to this endpoint (unless
    // state = cancelled), because the order seller should only change the state of
    // his items,
    // not the whole order, so we are only checking if the
    // user id is the same as the order buyer id , in order cancellation case only

    public void updateOrderState(@RequestHeader("Authorization") String authHeader, @PathVariable UUID orderId,
            @RequestBody String newState) {

        if (newState.equals(OrderStatus.CANCELLED.name())) {
            validateeUserIsOrderBuyer(authHeader, orderId);
        }
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
        } catch (IllegalStateException e) {
            // will be thrown by the "setOrderState" method / "cancelOrder" method
            // in the Order class if the operation is not allowed
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    e.getMessage());
        }
    }

    @PutMapping("/{orderId}/items/{productId}/editNote")
    public void updateItemNote(@RequestHeader("Authorization") String authHeader, @PathVariable UUID orderId,
            @PathVariable UUID productId,
            @RequestBody String note) {
        validateUserIsItemSeller(authHeader, orderId, productId);
        orderService.updateItemNote(orderId, productId, note);
    }

    @PostMapping("/{orderId}/reorder")
    public ResponseEntity<String> reorder(@RequestHeader("Authorization") String authHeader, @PathVariable String orderId) {
        validateeUserIsOrderBuyer(authHeader, UUID.fromString(orderId));
        orderService.reOrderAndSendItemsToCart(UUID.fromString(orderId));
        return ResponseEntity.ok("Order with id " + orderId + " has been reordered successfully");
    }

    private void validateeUserIsOrderBuyer(String authHeader, UUID orderId) {
        String userId = getUserField(authHeader, "id");
        Order order = orderService.getOrderById(orderId);
        String orderBuyerId = order.getBuyerId().toString();
        if (!orderBuyerId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to access this order");
        }
    }

    private void validateUserIsItemSeller(String authHeader, UUID orderId, UUID productId) {
        // only the seller of the product in the order can edit a note on his item
        String userId = getUserField(authHeader, "id");
        Order order = orderService.getOrderById(orderId);
        String productSellerId = order.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Product with id " + productId + " not found in order"))
                .getSellerId()
                .toString();
        if (!productSellerId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "you are not the seller of this product, so we can't let you edit a note on it");
        }

    }

    private String getUserField(String authHeader, String field) {
        return jwtUtil.getUserClaims(authHeader.replace("Bearer ", "")).get(field).toString();
    }
}
