package com.homechef.OrderService.clients;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// TODO: the current service url should be figured out by service discovery
@FeignClient(name = "auth-service", url = "${auth-service.url}")
public interface AuthServiceClient {
    @PostMapping("/fetch-emails")
    Map<String, String> getUsersEmails(@RequestBody List<UUID> userIds);
}
