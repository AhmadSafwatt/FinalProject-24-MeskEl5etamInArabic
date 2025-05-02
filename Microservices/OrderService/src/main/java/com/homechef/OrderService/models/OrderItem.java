package com.homechef.OrderService.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@IdClass(OrderItem.OrderItemPK.class) // composite key
public class OrderItem {
    @Id
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    @JsonIgnore
    private Order order;
    @Id
    private String productId;

    private String sellerId;
    private Integer quantity;
    private String notes;
    private Double totalPrice;




    // composite key
    public static class OrderItemPK implements Serializable {
        private UUID order;
        private String productId;
        public OrderItemPK() {}
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderItemPK that = (OrderItemPK) o;
            return Objects.equals(order, that.order) && Objects.equals(productId, that.productId);
        }
        @Override public int hashCode() { return Objects.hash(order, productId);}
    }
}
