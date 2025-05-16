package com.homechef.ProductService.DTO;

import java.util.UUID;

public class ProductMessage {
    UUID productId;
    int amount;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
