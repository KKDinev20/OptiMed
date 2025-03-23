package com.optimed.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "notification-service", url = "http://localhost:8081/api/notifications")
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestParam String email, @RequestParam String message);
}
