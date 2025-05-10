package com.homechef.ProductService.model;

import org.springframework.data.annotation.TypeAlias;

import java.util.UUID;
@TypeAlias("beverage")
public class Beverage extends Product {

    private boolean isCarbonated;
    private boolean isHot;

    public  Beverage(){

    }
    private Beverage(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = builder.name;
        this.sellerId = builder.sellerId;
        this.price = builder.price;
        this.amountSold = builder.amountSold;
        this.description=builder.description;
        this.discount=builder.discount;
        this.isCarbonated = builder.isCarbonated;
        this.isHot = builder.isHot;
    }
    public boolean isCarbonated() {
        return isCarbonated;
    }
    public boolean isHot() {
        return isHot;
    }

    @Override
    public String getType() {
        return "Beverage";
    }

    public static class Builder {
        private UUID id = UUID.randomUUID();
        private String name;
        private UUID sellerId;
        private Double price;
        private int amountSold;
        private String description;
        private Double discount;
        private boolean isCarbonated;
        private boolean isHot;

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
        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDiscount(Double discount) {
            this.discount = discount;
            return this;
        }
        public Builder setIsCarbonated(boolean isCarbonated) {
            this.isCarbonated = isCarbonated;
            return this;
        }
        public Builder setIsHot(boolean isHot) {
            this.isHot = isHot;
            return this;
        }
        public Beverage build() {
            return new Beverage(this);
        }
    }
}
