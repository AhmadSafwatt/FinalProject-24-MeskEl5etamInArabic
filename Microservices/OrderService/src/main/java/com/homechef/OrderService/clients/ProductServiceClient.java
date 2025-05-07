package com.homechef.OrderService.clients;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

// TODO: the current service url should be figured out by service discovery
@FeignClient(name = "product-service", url = "http://localhost:8085")
public interface ProductServiceClient {
    // TODO: the specific endpoint should be modified when safwat team implements it
    @PostMapping("products/{productId}/sales/edit/{quantity}")
    void modifyProductSales(@PathVariable("productId") UUID productId, @PathVariable("quantity") int quantity);
}
