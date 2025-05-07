package com.homechef.ProductService.model;

import org.springframework.data.annotation.TypeAlias;

import java.util.UUID;
@TypeAlias("food")
public class Food extends Product {

    public Food() {
        this.id = UUID.randomUUID();
    }
    public Food(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = builder.name;
        this.sellerId = builder.sellerId;
        this.price = builder.price;
        this.amountSold = builder.amountSold;
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
        private int amountSold;

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
        public Builder setAmountSold(int amountSold) {
            this.amountSold = amountSold;
            return this;
        }

        public Food build() {
            return new Food(this);
        }
    }
}