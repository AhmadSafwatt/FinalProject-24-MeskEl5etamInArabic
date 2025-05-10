package com.homechef.OrderService;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class HosainTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/orders/";

        // Clear the database before each test
        orderRepository.deleteAll();

        // Create a sample order
        testOrder = new Order();
        testOrder.setBuyerId(UUID.randomUUID());
        testOrder.setStatus(OrderStatus.CREATED);
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    void testGetOrderById_OrderExists() {
        String url = baseUrl + testOrder.getId();
        ResponseEntity<Order> response = restTemplate.getForEntity(url, Order.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testOrder.getId(), response.getBody().getId());
    }

    @Test
    void testGetOrder_OrderDoesNotExist() {
        UUID nonExistentOrderId = UUID.randomUUID();
        String url = baseUrl + nonExistentOrderId;
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 NOT_FOUND, but received " + responseEntity.getStatusCode());

    }

    @Test
    void testDeleteOrder_OrderExists() {
        String url = baseUrl + testOrder.getId();
        restTemplate.delete(url);
        assertFalse(orderRepository.findById(testOrder.getId()).isPresent());
    }

    @Test
    void testDeleteOrder_OrderDoesNotExist() {
        String url = baseUrl + UUID.randomUUID();
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 NOT_FOUND, but received " + responseEntity.getStatusCode());
    }

    @Test
    void testOrderCancellation_exists() {
        String url = baseUrl + testOrder.getId() + "/newState";
        restTemplate.put(url, OrderStatus.CANCELLED.name());
        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    }

    @Test
    void testOrderCancellation_valid() {
        String url = baseUrl + testOrder.getId() + "/newState";
        restTemplate.put(url, OrderStatus.CANCELLED.name());

        Order updatedOrder = orderRepository.findById(testOrder.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
    }

    // @Test
    // void testUpdateOrderState_InvalidState() {
    // // Call the API with an invalid state
    // String url = baseUrl + testOrder.getId() + "/newState";

    // // Verify the response throws a 400 error
    // ResponseEntity<String> responseEntity = restTemplate.postForEntity(url,
    // "INVALID_STATE", String.class);
    // assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
    // "Expected HTTP status 400 BAD_REQUEST, but received " +
    // responseEntity.getStatusCode());
    // }

    // @Test
    // void testUpdateItemNote_OrderExists() {
    // // Add an item to the order
    // UUID productId = UUID.randomUUID();
    // testOrder.setItems(List.of(new OrderItem(testOrder, productId,
    // UUID.randomUUID(), 1, "Old Note")));
    // orderRepository.save(testOrder);

    // // Call the API to update the item note
    // String url = baseUrl + testOrder.getId() + "/items/" + productId +
    // "/editNote";
    // restTemplate.put(url, "New Note");

    // // Verify the item note is updated
    // Order updatedOrder =
    // orderRepository.findById(testOrder.getId()).orElse(null);
    // assertNotNull(updatedOrder);
    // assertEquals("New Note", updatedOrder.getItems().get(0).getNotes());
    // }

    // @Test
    // void testUpdateItemNote_OrderDoesNotExist() {
    // // Call the API with a non-existent order ID
    // String url = baseUrl + UUID.randomUUID() + "/items/" + UUID.randomUUID()
    // + "/editNote";

    // // Verify the response throws a 404 error
    // HttpClientErrorException exception =
    // assertThrows(HttpClientErrorException.class, () -> {
    // restTemplate.put(url, "New Note");
    // });
    // assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    // }
}