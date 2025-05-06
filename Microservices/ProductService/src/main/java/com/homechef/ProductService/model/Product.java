package com.homechef.ProductService.model;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "products")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Food.class, name = "Food"),
        @JsonSubTypes.Type(value = Beverage.class, name = "Beverage")
})
public abstract class Product {
    @Id
    UUID id;
    String name;
    UUID sellerId;
    Double price;



    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public UUID getSellerId() {
        return sellerId;
    }
    public Double getPrice() {
        return price;
    }

//    private Product(Builder builder) {
//        this.id = builder.id;
//        this.name = builder.name;
//        this.price = builder.price;
//        this.sellerId = builder.sellerId;
//
//
//    }
//
//
//    public static class Builder {
//        private UUID id;
//        private String name;
//        private UUID sellerId;
//        private Double price;
//
//
//        public Builder() {
//
//        }
//        public Builder(String name, UUID sellerId, Double price) {
//            this.id=UUID.randomUUID();
//            this.name = name;
//            this.sellerId = sellerId;
//            this.price = price;
//        }
//        public Builder(UUID id, String name, UUID sellerId, Double price) {
//            this.id = id;
//            this.name = name;
//            this.sellerId = sellerId;
//            this.price = price;
//        }
//
//
//
//        public Product build() {
//            return new Product(this);
//        }
    //}



    public abstract String getType();


}
