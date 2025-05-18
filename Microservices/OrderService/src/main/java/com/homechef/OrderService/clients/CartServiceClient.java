package com.homechef.OrderService.clients;

import com.homechef.OrderService.DTOs.CartMessage;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "cart-service", url = "${cart-service.url}")
public interface CartServiceClient {

    @PostMapping("/reorder")
    void reorder(@RequestBody CartMessage cartMessage);
}
