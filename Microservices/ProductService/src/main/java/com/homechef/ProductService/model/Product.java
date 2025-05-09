package com.homechef.ProductService.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

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
    @Field(targetType = FieldType.STRING)
    UUID id;
    String name;
    @Field(targetType = FieldType.STRING)
    UUID sellerId;
    Double price;
    int amountSold;
    String description;
    Double discount ;


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
        return price  ;
    }

    public String getDescription() {
        return description;
    }

    public Double getDiscount() {
        return discount;
    }

    @JsonIgnore
    public abstract String getType();


}
