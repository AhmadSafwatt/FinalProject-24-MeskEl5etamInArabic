package com.homechef.OrderService.services;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;
import com.homechef.OrderService.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class OrdersSeeder {
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrdersSeeder(OrderRepository orderRepository) {this.orderRepository = orderRepository;}

    public List<Order> generateOrdersWithSomeCommonSellerIDsAndCommonBuyer(){

        String [] buyersIds = {
                "ae73c3fd-f444-4a54-9822-fa24ab6747c3",
                "2f3dc195-587d-4a6a-8da1-b0587c4a2310",
                "1fc3a55a-960a-4c94-bf83-a0c262d4e514",
                "d01a8a5e-544b-4c03-bb2e-c146c6e9e391",
                "2c3868e2-ac2a-414f-a2b8-8c9caeb9cb56"
        };

        UUID [] buyerUuids = Stream.of(buyersIds).map(UUID::fromString).toArray(UUID[]::new);


        Map<UUID, UUID> Product_Seller = Map.of(
                UUID.fromString("16b9eb22-301d-4ee9-bb21-88d9f52d08e0"), UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13"),
                UUID.fromString("0248f6fe-33a9-4585-b23f-e7ddf7aab32f"), UUID.fromString("e24f4ca0-d6d5-4a91-b453-45dde29067d5"),
                UUID.fromString("da85f125-3a17-4eab-a28f-a8ec2c9e18d8"), UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13"),
                UUID.fromString("80b99e96-4bf6-44bc-a691-10c3212a7ffb"), UUID.fromString("e24f4ca0-d6d5-4a91-b453-45dde29067d5")
        );

        List<Order> orders = new ArrayList<>();
        int ordersCount = (int) (Math.random() * 30 + 30); // Random number of orders between 30 and 60
        for (int i = 0; i < ordersCount; i++) {
            Order order = new Order();
            // get a random buyer id from the list
            UUID buyerId = buyerUuids[(int) (Math.random() * buyerUuids.length)];
            order.setBuyerId(buyerId);
            order.setStatus(OrderStatus.CREATED);
            order.setOrderDate(LocalDateTime.now());
            order.setOrderNote("Order #" + i + " - " + generateRandomString());
            int numberOfItems = Math.max(1, (int) (Math.random() * Product_Seller.size() - 1)); // Random number of items between 1 and size of Product_Seller
            List<OrderItem> items = new ArrayList<>();
            for (int j = 0; j < numberOfItems; j++) {
                OrderItem item = new OrderItem();
                item.setOrder(order);

                // get a random product id from the map
                   // Fixed version
                Map.Entry<UUID, UUID> entry;
                int attempts = Product_Seller.size();
                do {
                   attempts--;
                   entry = Product_Seller.entrySet().stream()
                           .skip((int) (Math.random() * Product_Seller.size()))
                           .findFirst()
                           .orElse(null);

                   // Only check for duplicates if entry is not null
                   if (entry != null) {
                       final UUID productId = entry.getKey();
                       if (items.stream().anyMatch(itemcheck -> itemcheck.getProductId().equals(productId))) {
                           // If duplicate found, set entry to null to try again
                           entry = null;
                       }
                   }
                } while (entry == null && attempts > 0);
                if (entry == null) break; // No valid entry found after max attempts
                item.setProductId(entry.getKey());
                item.setSellerId(entry.getValue());

                item.setQuantity((int) (Math.random() * 3) + 1); // Random quantity between 1 and 10
                item.setNotes(j + generateRandomString());
    //                item.setTotalPrice(Math.random() * 100); // Random price ... // no more price in OrderItem (for now)
                items.add(item);
            }
            order.setItems(items);
            orders.add(order);
        }
        return orders;
    }

    public String generateRandomString() {
        int length = (int) (Math.random() * 20 + 8); // Random length between 8 and 28
        StringBuilder sb = new StringBuilder(length);
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 ";
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public void seed() {
        List<Order> orders = generateOrdersWithSomeCommonSellerIDsAndCommonBuyer();
        orderRepository.saveAll(orders);
    }
}
