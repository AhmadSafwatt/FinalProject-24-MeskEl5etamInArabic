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
    private UUID productId;

    private UUID sellerId;
    private Integer quantity;
    private String notes;
    private Double totalPrice;



    public OrderItem() {}
    public OrderItem(Order order, UUID productId, UUID sellerId, Integer quantity, String notes, Double totalPrice) {
        this.order = order;
        this.productId = productId;
        this.sellerId = sellerId;
        this.quantity = quantity;
        this.notes = notes;
        this.totalPrice = totalPrice;
    }

    public Order getOrder() {return order;}
    public void setOrder(Order order) { this.order = order; }
    public UUID getProductId() {return productId;}
    public void setProductId(UUID productId) {this.productId = productId;}
    public UUID getSellerId() {return sellerId;}
    public void setSellerId(UUID sellerId) {this.sellerId = sellerId;}
    public Integer getQuantity() {return quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}
    public Double getTotalPrice() {return totalPrice;}
    public void setTotalPrice(Double totalPrice) {this.totalPrice = totalPrice;}

    // composite key
    public static class OrderItemPK implements Serializable {
        private UUID order;
        private UUID productId;
        public OrderItemPK() {}
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OrderItemPK that = (OrderItemPK) o;
            return Objects.equals(order, that.order) && Objects.equals(productId, that.productId);
        }
        @Override public int hashCode() { return Objects.hash(order, productId);}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderItem orderItem)) return false;
        return Objects.equals(order, orderItem.order) && Objects.equals(productId, orderItem.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, productId);
    }

    @Override
    public String toString() {
        return "OrderItem{" +
                "order=" + order +
                ", productId=" + productId +
                ", sellerId=" + sellerId +
                ", quantity=" + quantity +
                ", notes='" + notes + '\'' +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
