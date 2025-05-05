package com.homechef.OrderService;

import com.homechef.OrderService.controllers.OrderController;
import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private UUID testOrderId;
    private UUID testBuyerId;
    private UUID testSellerId, testSellerId2;
    private UUID testProductId;
    private Order testOrder;
    private OrderItem testOrderItem, testOrderItem2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testOrderId = UUID.randomUUID();
        testBuyerId = UUID.randomUUID();
        testSellerId = UUID.randomUUID();
        testSellerId2 = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        testOrderItem = new OrderItem();
        testOrderItem.setProductId(testProductId);
        testOrderItem.setSellerId(testSellerId);
        testOrderItem.setQuantity(2);
        testOrderItem.setNotes("Test notes");
        testOrderItem.setTotalPrice(20.0);

        testOrderItem2 = new OrderItem();
        testOrderItem2.setProductId(testProductId);
        testOrderItem2.setSellerId(testSellerId);
        testOrderItem2.setQuantity(3);
        testOrderItem2.setNotes("Test notes 2");
        testOrderItem2.setTotalPrice(30.0);


        testOrder = new Order();
        testOrder.setId(testOrderId);
        testOrder.setBuyerId(testBuyerId);
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder.setItems(List.of(testOrderItem, testOrderItem2));
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderService.getAllOrders()).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderController.getAllOrders();

        // Assert
        assertEquals(expectedOrders, result);
        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void createOrder_ShouldReturnCreatedOrder() {
        // Arrange
        when(orderService.createOrder(testOrder.getBuyerId(),testOrder.getStatus(), testOrder.getItems())).thenReturn(testOrder);

        // Act
        Order result = orderController.createOrder(testOrder);

        // Assert
        assertEquals(testOrder, result);
        verify(orderService, times(1)).createOrder(testOrder.getBuyerId(),testOrder.getStatus(), testOrder.getItems());
    }

    @Test
    void getAllOrdersByBuyerId_ShouldReturnBuyerOrders() {
        // Arrange
        List<Order> expectedOrders = Arrays.asList(testOrder);
        when(orderService.getAllOrdersByBuyerId(testBuyerId)).thenReturn(expectedOrders);

        // Act
        List<Order> result = orderController.getAllOrdersByBuyerId(testBuyerId.toString());

        // Assert
        assertEquals(expectedOrders, result);
        verify(orderService, times(1)).getAllOrdersByBuyerId(testBuyerId);
        System.out.println("Buyer ID: " + testBuyerId);
        System.out.println(expectedOrders);
    }
    @Test
    void getFilteredOrdersBySellerId_ShouldReturnOnlyOrdersWithItemsForThatSeller() {
        // Create a filtered version of the order with only items for seller1
        Order filteredOrderForSeller1 = new Order();
        filteredOrderForSeller1.setId(testOrderId);
        filteredOrderForSeller1.setBuyerId(testBuyerId);
        filteredOrderForSeller1.setStatus(OrderStatus.CREATED);
        filteredOrderForSeller1.setItems(List.of(testOrderItem));

        // Create a filtered version of the order with only items for seller2
        Order filteredOrderForSeller2 = new Order();
        filteredOrderForSeller2.setId(testOrderId);
        filteredOrderForSeller2.setBuyerId(testBuyerId);
        filteredOrderForSeller2.setStatus(OrderStatus.CREATED);
        filteredOrderForSeller2.setItems(List.of(testOrderItem2));

        // Test for seller1
        when(orderService.getFilteredOrdersBySellerId(testSellerId))
                .thenReturn(List.of(filteredOrderForSeller1));

        List<Order> resultForSeller1 = orderController.getFilteredOrdersBySellerId(testSellerId.toString());
        assertEquals(1, resultForSeller1.size());
        assertEquals(1, resultForSeller1.get(0).getItems().size());
        assertEquals(testSellerId, resultForSeller1.get(0).getItems().get(0).getSellerId());

        // Test for seller2
        when(orderService.getFilteredOrdersBySellerId(testSellerId2))
                .thenReturn(List.of(filteredOrderForSeller2));

        List<Order> resultForSeller2 = orderController.getFilteredOrdersBySellerId(testSellerId2.toString());
        assertEquals(1, resultForSeller2.size());
        assertEquals(1, resultForSeller2.get(0).getItems().size());
        assertEquals(testSellerId2, resultForSeller2.get(0).getItems().get(0).getSellerId());

        verify(orderService, times(1)).getFilteredOrdersBySellerId(testSellerId);
        verify(orderService, times(1)).getFilteredOrdersBySellerId(testSellerId2);
    }

    @Test
    void getOrderByIdFilteredBySellerId_ShouldReturnOnlyItemsForRequestedSeller() throws IllegalAccessException {
        // Create filtered versions of the order
        Order filteredOrderForSeller1 = new Order();
        filteredOrderForSeller1.setId(testOrderId);
        filteredOrderForSeller1.setBuyerId(testBuyerId);
        filteredOrderForSeller1.setStatus(OrderStatus.CREATED);
        filteredOrderForSeller1.setItems(List.of(testOrderItem));

        // Mock the service to return filtered order for seller1
        when(orderService.getOrderByIdFilteredBySellerId(testOrderId, testSellerId))
                .thenReturn(filteredOrderForSeller1);

        Order resultForSeller1 = orderController.getOrderByIdFilteredBySellerId(
                testOrderId.toString(),
                testSellerId.toString()
        );

        // Verify only seller1's items are included
        assertEquals(1, resultForSeller1.getItems().size());
        assertEquals(testSellerId, resultForSeller1.getItems().get(0).getSellerId());

        // Verify the service was called correctly
        verify(orderService, times(1))
                .getOrderByIdFilteredBySellerId(testOrderId, testSellerId);
    }

    @Test
    void getOrderByIdFilteredBySellerId_ShouldThrowWhenNoItemsForSeller() throws IllegalAccessException {
        // Mock the service to throw when no items for seller
        UUID nonMatchingSellerId = UUID.randomUUID();
        when(orderService.getOrderByIdFilteredBySellerId(testOrderId, nonMatchingSellerId))
                .thenThrow(new IllegalAccessException("No items for this seller"));

        // Verify exception is thrown
        assertThrows(IllegalAccessException.class, () -> {
            orderController.getOrderByIdFilteredBySellerId(
                    testOrderId.toString(),
                    nonMatchingSellerId.toString()
            );
        });

        verify(orderService, times(1))
                .getOrderByIdFilteredBySellerId(testOrderId, nonMatchingSellerId);
    }
}