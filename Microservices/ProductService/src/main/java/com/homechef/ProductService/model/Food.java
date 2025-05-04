package com.homechef.ProductService.model;

import org.springframework.data.annotation.TypeAlias;

import java.util.UUID;

public class Food extends Product {

    public Food() {
        this.id = UUID.randomUUID();
    }
    public Food(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = builder.name;
        this.sellerId = builder.sellerId;
        this.price = builder.price;
    }

    @Override
    public String getType() {
        return "Food";
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private UUID sellerId;
        private Double price;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setSellerId(UUID sellerId) {
            this.sellerId = sellerId;
            return this;
        }

        public Builder setPrice(Double price) {
            this.price = price;
            return this;
        }

        public Food build() {
            return new Food(this);
        }
    }
}