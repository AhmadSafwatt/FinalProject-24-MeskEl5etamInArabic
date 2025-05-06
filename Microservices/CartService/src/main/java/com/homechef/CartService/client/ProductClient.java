package com.homechef.CartService.client;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "product-service", url = "http://localhost:8085")
public interface ProductClient {
    
}
