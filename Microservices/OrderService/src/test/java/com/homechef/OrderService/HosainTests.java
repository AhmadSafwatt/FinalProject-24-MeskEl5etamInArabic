package com.homechef.OrderService;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.repositories.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    private UUID fixedProductId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/orders/";
        fixedProductId = UUID.randomUUID();

        orderRepository.deleteAll();

        testOrder = new Order();
        testOrder.setBuyerId(UUID.randomUUID());
        testOrder.setStatus(OrderStatus.CREATED);

        OrderItem item = new OrderItem();
        item.setProductId(fixedProductId);
        item.setQuantity(1);
        item.setNotes("Initial Note");
        testOrder.addItem(item);

        testOrder = orderRepository.save(testOrder);
    }

    // ------------------------------------- get order by id
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

    // ------------------------------------- delete order

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

    // ------------------------ update order state (order exists does not exist)
    @Test
    void testUpdateOrderState_OrderDoesNotExist() {
        UUID nonExistentOrderId = UUID.randomUUID();
        String url = baseUrl + nonExistentOrderId + "/newState";
        HttpEntity<String> requestEntity = new HttpEntity<>(OrderStatus.PREPARING.name());
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 NOT_FOUND when attempting to update state of a non-existent order, but received "
                        + responseEntity.getStatusCode());
    }

    // ------------------------ update order state (invalid state string)
    @Test
    void testUpdateOrderState_InvalidStateString() {
        String url = baseUrl + testOrder.getId() + "/newState";
        HttpEntity<String> requestEntity = new HttpEntity<>("I_am_invlid_state_string");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                "Expected HTTP status 400 BAD_REQUEST when attempting to update state with an invalid state string, but received "
                        + responseEntity.getStatusCode());
    }

    // ----------------- update order state & cancel order (check state transition
    // validity)
    private void assertStateTransition(OrderStatus initialStatus, OrderStatus targetStatus,
            boolean expectValidTransition) {
        testOrder.setStatus(initialStatus);
        orderRepository.save(testOrder);
        String url = baseUrl + testOrder.getId() + "/newState";
        HttpEntity<String> requestEntity = new HttpEntity<>(targetStatus.name());
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);

        if (expectValidTransition) {
            assertEquals(HttpStatus.OK, responseEntity.getStatusCode(),
                    "Expected HTTP OK for valid transition from " + initialStatus + " to " + targetStatus);
            Order updatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(targetStatus, updatedOrder.getStatus(),
                    "Order status should be " + targetStatus + " after valid transition from " + initialStatus);
        } else {
            assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                    "Expected HTTP BAD_REQUEST for invalid transition from " + initialStatus + " to " + targetStatus);
            Order notUpdatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(initialStatus, notUpdatedOrder.getStatus(),
                    "Order status should remain " + initialStatus + " after invalid transition to " + targetStatus);
        }
    }

    // --- Transitions from CREATED ---
    @Test
    void testUpdateOrderState_Created_to_Preparing_Valid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.PREPARING, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Prepared_Valid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Created_to_OutForDelivery_Valid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.OUT_FOR_DELIVERY, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Cancelled_Valid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.CANCELLED, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Delivered_Invalid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Created_to_Created_Invalid() {
        assertStateTransition(OrderStatus.CREATED, OrderStatus.CREATED, false);
    }

    // --- Transitions from PREPARING ---
    @Test
    void testUpdateOrderState_Preparing_to_Prepared_Valid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.PREPARED, true);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Created_Invalid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_OutForDelivery_Invalid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Delivered_Invalid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Cancelled_Invalid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Preparing_Invalid() {
        assertStateTransition(OrderStatus.PREPARING, OrderStatus.PREPARING, false);
    }

    // --- Transitions from PREPARED ---
    @Test
    void testUpdateOrderState_Prepared_to_OutForDelivery_Valid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.OUT_FOR_DELIVERY, true);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Created_Invalid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Preparing_Invalid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Delivered_Invalid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Cancelled_Invalid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Prepared_Invalid() {
        assertStateTransition(OrderStatus.PREPARED, OrderStatus.PREPARED, false);
    }

    // --- Transitions from OUT_FOR_DELIVERY ---
    @Test
    void testUpdateOrderState_OutForDelivery_to_Delivered_Valid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, true);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Cancelled_Valid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Created_Invalid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Preparing_Invalid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Prepared_Invalid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_OutForDelivery_Invalid() {
        assertStateTransition(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    // --- Transitions from DELIVERED ---
    @Test
    void testUpdateOrderState_Delivered_to_Created_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Preparing_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Prepared_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_OutForDelivery_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Cancelled_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Delivered_Invalid() {
        assertStateTransition(OrderStatus.DELIVERED, OrderStatus.DELIVERED, false);
    }

    // --- Transitions from CANCELLED ---
    @Test
    void testUpdateOrderState_Cancelled_to_Created_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Preparing_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Prepared_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_OutForDelivery_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Delivered_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Cancelled_Invalid() {
        assertStateTransition(OrderStatus.CANCELLED, OrderStatus.CANCELLED, false);
    }

    // update item note (order does not exists)
    @Test
    void testUpdateItemNote_OrderDoesNotExist() {
        UUID nonExistentOrderId = UUID.randomUUID();
        String url = baseUrl + nonExistentOrderId + "/items/" + fixedProductId + "/editNote";
        HttpEntity<String> requestEntity = new HttpEntity<>("New Note");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 NOT_FOUND when attempting to update item note for a non-existent order, but received "
                        + responseEntity.getStatusCode());
    }

    // update item note (order exists, item does not exist)
    @Test
    void testUpdateItemNote_ItemDoesNotExist() {
        String url = baseUrl + testOrder.getId() + "/items/" + UUID.randomUUID() + "/editNote";
        HttpEntity<String> requestEntity = new HttpEntity<>("New Note");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                "Expected HTTP status 400 BAD_REQUEST when attempting to update item note for a non-existent item, but received "
                        + responseEntity.getStatusCode());
    }

    // ---------------------- update item note (check state while updating)
    private void assertItemNoteUpdateAttempt(OrderStatus orderStatus, String newNote, boolean expectSuccess) {
        Order orderForTestSetup = orderRepository.findByIdWithItems(this.testOrder.getId())
                .orElseThrow(() -> new AssertionError(
                        "Initial testOrder not found in DB (with items) before setting status. ID: "
                                + this.testOrder.getId()));

        String oldNote = orderForTestSetup.getItems().stream()
                .filter(i -> i.getProductId().equals(fixedProductId))
                .findFirst()
                .map(OrderItem::getNotes)
                .orElseThrow(
                        () -> new AssertionError("Test item for oldNote not found. Product ID: " + fixedProductId));

        assertNotEquals(oldNote, newNote,
                "in testing, Old note should not be the same as new note, so please enter a different input note for testing");

        orderForTestSetup.setStatus(orderStatus);
        orderRepository.save(orderForTestSetup);

        String url = baseUrl + orderForTestSetup.getId() + "/items/" + fixedProductId + "/editNote";
        HttpEntity<String> requestEntity = new HttpEntity<>(newNote);
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);

        HttpStatus expected_HttpStatus = expectSuccess ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        assertEquals(expected_HttpStatus, responseEntity.getStatusCode(),
                "Expected HTTP status " + expected_HttpStatus + " when updating note for order in " + orderStatus
                        + " state, but received " + responseEntity.getStatusCode());

        Order updatedOrderFromDb = orderRepository.findByIdWithItems(orderForTestSetup.getId())
                .orElseThrow(() -> new AssertionError(
                        "Order not found in DB (with items) after API call. ID: " + orderForTestSetup.getId()));

        OrderItem updatedItem = updatedOrderFromDb.getItems().stream()
                .filter(item -> item.getProductId().equals(fixedProductId))
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError("Test item not found after update. Product ID: " + fixedProductId));

        String expectedNoteInDb = expectSuccess ? newNote : oldNote;
        assertEquals(expectedNoteInDb, updatedItem.getNotes(),
                "Item note in DB should be '" + expectedNoteInDb + "' for order in " + orderStatus + " state. Actual: '"
                        + updatedItem.getNotes() + "'");
    }

    @Test
    void testUpdateItemNote_When_OrderIs_Created_ShouldSucceed() {
        assertItemNoteUpdateAttempt(OrderStatus.CREATED, "Note updated in CREATED state", true);
    }

    @Test
    void testUpdateItemNote_When_OrderIs_Preparing_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.PREPARING, "Note updated in PREPARING state", false);
    }

    @Test
    void testUpdateItemNote_When_OrderIs_Prepared_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.PREPARED, "Note updated in PREPARED state", false);
    }

    @Test
    void testUpdateItemNote_When_OrderIs_OutForDelivery_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.OUT_FOR_DELIVERY, "Note updated in OUT_FOR_DELIVERY state", false);
    }

    @Test
    void testUpdateItemNote_When_OrderIs_Delivered_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.DELIVERED, "Note updated in DELIVERED state", false);
    }

    @Test
    void testUpdateItemNote_When_OrderIs_Cancelled_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.CANCELLED, "Note updated in CANCELLED state", false);
    }
}