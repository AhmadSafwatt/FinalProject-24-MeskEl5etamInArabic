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
import java.util.UUID;

@Component
public class OrdersSeeder {
    private final OrderRepository orderRepository;
    
    @Autowired
    public OrdersSeeder(OrderRepository orderRepository) {this.orderRepository = orderRepository;}

    public List<Order> generateOrdersWithSomeCommonSellerIDsAndCommonBuyer(){
        UUID commonSellerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID commonBuyerId = UUID.fromString("10000000-0000-0000-0000-000000000002");
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            Order order = new Order();
            order.setBuyerId((Math.random() > 0.5) ? commonBuyerId : UUID.randomUUID());
            order.setStatus(OrderStatus.CREATED);
            order.setOrderDate(LocalDateTime.now());
            int numberOfItems = (int) (Math.random() * 5) + 1; // Random number of items between 1 and 5
            List<OrderItem> items = new ArrayList<>();
            for (int j = 0; j < numberOfItems; j++) {
                OrderItem item = new OrderItem();
                item.setOrder(order);
                item.setProductId(UUID.randomUUID());
                item.setSellerId((Math.random() > 0.5) ? commonSellerId : UUID.randomUUID());
                item.setQuantity((int) (Math.random() * 10) + 1); // Random quantity between 1 and 10
                item.setNotes("Sample note " + j);
//                item.setTotalPrice(Math.random() * 100); // Random price
                items.add(item);
            }
            order.setItems(items);
            orders.add(order);
        }
        return orders;
    }

    public void seed() {
        List<Order> orders = generateOrdersWithSomeCommonSellerIDsAndCommonBuyer();
        orderRepository.saveAll(orders);
    }
}
