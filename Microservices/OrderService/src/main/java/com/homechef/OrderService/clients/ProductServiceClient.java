package com.homechef.OrderService.clients;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

// TODO: the current service url should be figured out by service discovery
@FeignClient(name = "product-service", url = "${products-service.url}")
public interface ProductServiceClient {
    // TODO: the specific endpoint should be modified when safwat team implements it
    @PutMapping("/{id}/decrement")
    void decrementAmountSold(@PathVariable String id, @RequestParam int amount);
}
