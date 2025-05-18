package com.homechef.OrderService.repositories;

import com.homechef.OrderService.models.Order;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findAllByBuyerId(UUID buyerId);

    @Query(nativeQuery = true, value = "SELECT * FROM orders o WHERE :sellerId IN" +
            "(SELECT seller_id FROM order_item WHERE order_item.order_id = o.id)")
    List<Order> findAllOrdersContainingSellerId(@Param("sellerId") UUID sellerId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") UUID orderId);
}
