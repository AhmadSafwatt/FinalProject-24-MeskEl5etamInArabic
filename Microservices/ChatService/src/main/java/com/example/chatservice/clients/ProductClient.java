package com.example.chatservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Component
@FeignClient(name = "product-service", url = "${products-service.url}")
public interface ProductClient {
    @GetMapping("/products/{id}")
    String getProductById(@PathVariable("id") String id);
}