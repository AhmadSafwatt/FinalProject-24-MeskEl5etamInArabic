package com.homechef.CartService.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "carts")
public class Cart {

    @Id
    @Field(targetType = FieldType.STRING)   // force mongo to store the UUID as a string
    UUID id;
    @Field(targetType = FieldType.STRING)
    UUID customerId;
    List<CartItem> cartItems;
    String notes;
    boolean promo;

    private Cart(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customer_id;
        this.cartItems = builder.cartItems;
        this.notes = builder.notes;
        this.promo = builder.promo;
    }

    public Cart() {
    }

//    add/remove product to cart


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isPromo() {
        return promo;
    }

    public void setPromo(boolean promo) {
        this.promo = promo;
    }

    public static class Builder {
        private UUID id;
        private UUID customer_id;
        private List<CartItem> cartItems = new ArrayList<>();
        private String notes;
        private boolean promo;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder customerId(UUID customer_id) {
            this.customer_id = customer_id;
            return this;
        }

        public Builder addCartItem(CartItem item) {
            this.cartItems.add(item);
            return this;
        }

        public Builder cartItems(List<CartItem> items) {
            this.cartItems = items;
            return this;
        }

        public Builder notes(String notes){
            this.notes = notes;
            return this;
        }

        public Builder promo(boolean promo){
            this.promo = promo;
            return this;
        }

        public Cart build() {
            return new Cart(this);
        }

        public Builder from(Cart existing) {
            this.id = existing.getId();
            this.customer_id = existing.getCustomerId();
            this.cartItems = existing.getCartItems();
            this.notes = existing.getNotes();
            return this;
        }
    }
}
