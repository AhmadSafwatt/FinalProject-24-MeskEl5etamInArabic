package com.example.chatservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "message-service", url = "http://localhost:8085")
public interface MessageClient {

    @GetMapping("/messages/{id}")
    Object getMessageById(@PathVariable("id") String id);

}