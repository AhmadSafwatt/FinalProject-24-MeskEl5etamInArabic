package com.homechef.OrderService;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.repositories.OrderRepository;
import com.homechef.OrderService.config.JwtUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

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

import org.springframework.http.HttpHeaders;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

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
    private UUID fixedSellerId;

    private SecretKey getSignKeyFromSecretString(String secretString) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secretString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateTestToken(String userId, String role) {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("id", userId);
        claimsMap.put("role", role);

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date exp = new Date(nowMillis + 1000 * 60 * 60); // Token valid for 1 hour

        claimsMap.put(Claims.SUBJECT, userId);
        claimsMap.put(Claims.ISSUED_AT, now);
        claimsMap.put(Claims.EXPIRATION, exp);

        return Jwts.builder()
                .claims(claimsMap)
                .signWith(getSignKeyFromSecretString(JwtUtil.SECRET))
                .compact();
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/orders/";
        fixedProductId = UUID.randomUUID();
        fixedSellerId = UUID.randomUUID();

        orderRepository.deleteAll();

        testOrder = new Order();
        testOrder.setBuyerId(UUID.randomUUID());
        testOrder.setStatus(OrderStatus.CREATED);

        OrderItem item = new OrderItem();
        item.setProductId(fixedProductId);
        item.setSellerId(fixedSellerId);
        item.setQuantity(1);
        item.setNotes("Initial Note");
        testOrder.addItem(item);

        testOrder = orderRepository.save(testOrder);
    }

    // ------------------------------------- get order by id
    // Non Authenticated
    @Test
    void testGetOrderById_notAuthenticated_UserIsBuyer_ReturnsBadRequest() {
        String url = baseUrl + testOrder.getId();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // Authenticated, user is buyer
    @Test
    void testGetOrderById_Authenticated_UserIsBuyer_ReturnsOk() {
        String userId = testOrder.getBuyerId().toString();
        HttpEntity<String> entity = generateHttpsEntity(userId, "user");
        String url = baseUrl + testOrder.getId();
        ResponseEntity<Order> response = restTemplate.exchange(url, HttpMethod.GET, entity, Order.class);

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testOrder.getId(), response.getBody().getId());
        assertEquals(testOrder.getBuyerId(), response.getBody().getBuyerId());
    }

    // Authenticated, user is not buyer
    @Test
    void testGetOrderById_Authenticated_UserIsnotBuyer_ReturnsForbidden() {
        String differentUserId = UUID.randomUUID().toString();
        HttpEntity<String> entity = generateHttpsEntity(differentUserId, "user");
        String url = baseUrl + testOrder.getId();
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ------------------------------------- update order state
    // Non Authenticated
    @Test
    void testUpdateOrderState_notAuthenticated_OrderExists_UserIsBuyer_ReturnsBadRequest() {
        String url = baseUrl + testOrder.getId() + "/newState";
        HttpEntity<String> requestEntity = new HttpEntity<>(OrderStatus.PREPARING.name());
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    // Authenticated, order doesn't exists
    @Test
    void testUpdateOrderState_Authenticated_OrderDoesNotExist_ReturnsNotFound() {
        UUID nonExistentOrderId = UUID.randomUUID();
        String url = baseUrl + nonExistentOrderId + "/newState";
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                OrderStatus.PREPARING.name(),
                UUID.randomUUID().toString(),
                "user");

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    // Authenticated (invalid state string)
    @Test
    void testUpdateOrderState_Authenticated_InvalidStateString_ReturnsBadRequest() {
        String url = baseUrl + testOrder.getId() + "/newState";
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                "INVALID_STATE",
                testOrder.getBuyerId().toString(),
                "user");

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url, HttpMethod.PUT, requestEntity, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    // --- (check state transition validity)
    private void assertStateTransition_Authorized(OrderStatus initialStatus, OrderStatus targetStatus,
            boolean validBuyer, boolean expectValidTransition, HttpStatus statusForNonValidTrans) {
        testOrder.setStatus(initialStatus);
        orderRepository.save(testOrder);
        String url = baseUrl + testOrder.getId() + "/newState";
        String userId = validBuyer ? testOrder.getBuyerId().toString() : UUID.randomUUID().toString();
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                targetStatus.name(),
                userId,
                "user");
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
            assertEquals(statusForNonValidTrans, responseEntity.getStatusCode(),
                    "Expected HTTP" + statusForNonValidTrans + "for invalid transition from " + initialStatus + " to "
                            + targetStatus);
            Order notUpdatedOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
            assertEquals(initialStatus, notUpdatedOrder.getStatus(),
                    "Order status should remain " + initialStatus + " after invalid transition to " + targetStatus);
        }
    }

    private void assertStateTransition_Authorized_ValidBuyer(OrderStatus initialStatus, OrderStatus targetStatus,
            boolean expectValidTransition) {
        assertStateTransition_Authorized(initialStatus, targetStatus, true, expectValidTransition,
                HttpStatus.BAD_REQUEST);
    }

    // test updating order status with VALID buyer

    // --- Transitions from CREATED ---
    @Test
    void testUpdateOrderState_Created_to_Preparing_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.PREPARING, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Prepared_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Created_to_OutForDelivery_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.OUT_FOR_DELIVERY, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Cancelled_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.CANCELLED, true);
    }

    @Test
    void testUpdateOrderState_Created_to_Delivered_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Created_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CREATED, OrderStatus.CREATED, false);
    }

    // --- Transitions from Preparing ---
    @Test
    void testUpdateOrderState_Preparing_to_Prepared_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.PREPARED, true);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_OutForDelivery_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Delivered_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Cancelled_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Preparing_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARING, OrderStatus.PREPARING, false);
    }

    // --- Transitions from PREPARED ---
    @Test
    void testUpdateOrderState_Prepared_to_OutForDelivery_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.OUT_FOR_DELIVERY, true);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Preparing_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Delivered_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Cancelled_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Prepared_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.PREPARED, OrderStatus.PREPARED, false);
    }

    // --- Transitions from OUT_FOR_DELIVERY ---
    @Test
    void testUpdateOrderState_OutForDelivery_to_Delivered_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, true);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Cancelled_Valid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Preparing_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Prepared_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_OutForDelivery_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    // --- Transitions from DELIVERED ---
    @Test
    void testUpdateOrderState_Delivered_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Preparing_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Prepared_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_OutForDelivery_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Cancelled_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.CANCELLED, false);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Delivered_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.DELIVERED, OrderStatus.DELIVERED, false);
    }

    // --- Transitions from CANCELLED ---
    @Test
    void testUpdateOrderState_Cancelled_to_Created_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.CREATED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Preparing_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.PREPARING, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Prepared_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.PREPARED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_OutForDelivery_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.OUT_FOR_DELIVERY, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Delivered_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.DELIVERED, false);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Cancelled_Invalid() {
        assertStateTransition_Authorized_ValidBuyer(OrderStatus.CANCELLED, OrderStatus.CANCELLED, false);
    }

    // test updating order status with NON VALID buyer, should return forbidden in
    // all cases
    private void assertStateTransition_Authorized_NonValidBuyer(OrderStatus initialStatus, OrderStatus targetStatus) {
        assertStateTransition_Authorized(initialStatus, targetStatus, false, false,
                HttpStatus.FORBIDDEN);
    }

    @Test
    void testUpdateOrderState_Created_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.CREATED, OrderStatus.CANCELLED);
    }

    @Test
    void testUpdateOrderState_Preparing_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.PREPARING, OrderStatus.CANCELLED);
    }

    @Test
    void testUpdateOrderState_Prepared_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.PREPARED, OrderStatus.CANCELLED);
    }

    @Test
    void testUpdateOrderState_OutForDelivery_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.CANCELLED);
    }

    @Test
    void testUpdateOrderState_Delivered_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.DELIVERED, OrderStatus.CANCELLED);
    }

    @Test
    void testUpdateOrderState_Cancelled_to_Cancelled_NonValidBuyer() {
        assertStateTransition_Authorized_NonValidBuyer(OrderStatus.CANCELLED, OrderStatus.CANCELLED);
    }

    // -------------------------------------------- update item note
    // non Authenticated
    @Test
    void testUpdateItemNote_notAuthenticated_OrderExists_ReturnsBadRequest() {
        String url = baseUrl + testOrder.getId() + "/items/" + fixedProductId + "/editNote";
        HttpEntity<String> requestEntity = new HttpEntity<>("New Note");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode(),
                "Expected HTTP status 400 BAD_REQUEST when attempting to update item note without authentication, but received "
                        + responseEntity.getStatusCode());
    }

    // Authenticated, order does not exist
    @Test
    void testUpdateItemNote_Authenticated_OrderDoesNotExist() {
        UUID nonExistentOrderId = UUID.randomUUID();
        String url = baseUrl + nonExistentOrderId + "/items/" + fixedProductId + "/editNote";
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                "New Note",
                fixedSellerId.toString(),
                "user");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 NOT_FOUND when attempting to update item note for a non-existent order, but received "
                        + responseEntity.getStatusCode());
    }

    // Authenticated, order exists but item does not exist
    @Test
    void testUpdateItemNote_Authenticated_ItemDoesNotExist() {
        String randomItemId = UUID.randomUUID().toString();
        String url = baseUrl + testOrder.getId() + "/items/" + randomItemId + "/editNote";
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                "New Note",
                fixedSellerId.toString(),
                "user");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Expected HTTP status 404 Not found when attempting to update item note for a non-existent item, but received "
                        + responseEntity.getStatusCode());
    }

    // Authenticated, order exists and item exists but user is not the seller of the
    // item
    @Test
    void testUpdateItemNote_Authenticated_UserIsNotSeller() {
        String url = baseUrl + testOrder.getId() + "/items/" + fixedProductId + "/editNote";
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                "New Note",
                UUID.randomUUID().toString(),
                "user");
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                String.class);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode(),
                "Expected HTTP status 403 FORBIDDEN when attempting to update item note as a non-seller, but received "
                        + responseEntity.getStatusCode());
    }

    // Authenticated, order exists, item exists, user is the seller, check state
    // while updating item note
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
        HttpEntity<String> requestEntity = generateHttpsEntityWithBody(
                newNote,
                fixedSellerId.toString(),
                "user");
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
    void testUpdateItemNote_Authenticated_When_OrderIs_Created_ShouldSucceed() {
        assertItemNoteUpdateAttempt(OrderStatus.CREATED, "Note updated in CREATED state", true);
    }

    @Test
    void testUpdateItemNote_Authenticated_When_OrderIs_Preparing_ShouldSucceed() {
        assertItemNoteUpdateAttempt(OrderStatus.PREPARING, "Note updated in PREPARING state", true);
    }

    @Test
    void testUpdateItemNote_Authenticated_When_OrderIs_Prepared_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.PREPARED, "Note updated in PREPARED state", false);
    }

    @Test
    void testUpdateItemNote_Authenticated_When_OrderIs_OutForDelivery_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.OUT_FOR_DELIVERY, "Note updated in OUT_FOR_DELIVERY state", false);
    }

    @Test
    void testUpdateItemNote_Authenticated_When_OrderIs_Delivered_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.DELIVERED, "Note updated in DELIVERED state", false);
    }

    @Test
    void testUpdateItemNote_Authenticated_When_OrderIs_Cancelled_ShouldFail() {
        assertItemNoteUpdateAttempt(OrderStatus.CANCELLED, "Note updated in CANCELLED state", false);
    }

    private HttpEntity<String> generateHttpsEntity(String userId, String role) {
        String token = generateTestToken(userId, "user");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(headers);
    }

    private <T> HttpEntity<T> generateHttpsEntityWithBody(T body, String userId, String role) {
        String token = generateTestToken(userId, role);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return new HttpEntity<>(body, headers);
    }

}