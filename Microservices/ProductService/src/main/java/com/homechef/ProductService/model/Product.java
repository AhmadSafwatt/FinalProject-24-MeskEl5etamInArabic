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
    int amountSold;


    public UUID getId() {
        return id;
    }

    public int getAmountSold() {
        return amountSold;
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





    public abstract String getType();


}
