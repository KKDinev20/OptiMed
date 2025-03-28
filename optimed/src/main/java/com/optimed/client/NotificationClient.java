package com.optimed.client;

import com.optimed.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.CollectionModel;

@FeignClient(name = "notification-service", url = "http://localhost:8081/api/notifications")
public interface NotificationClient {

    @PostMapping
    void sendNotification(@RequestParam String email, @RequestParam String message);

    @PostMapping("/register")
    void registerDoctorIfNotExists(@RequestParam String email);

    @GetMapping("/email/{email}")
    CollectionModel<NotificationRequest> getNotificationsByEmail(@PathVariable String email);
}
