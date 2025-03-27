package com.optimednotifications.controller;

import com.optimednotifications.service.*;
import com.optimednotifications.entity.*;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public EntityModel<Notification> sendNotification(@RequestParam String email, @RequestParam String message) {
        Notification notification = notificationService.createNotification(email, message);
        return EntityModel.of(notification,
                linkTo(methodOn(NotificationController.class).getNotifications(email)).withRel("recipient-notifications"));
    }

    @GetMapping("/email/{email}")
    public CollectionModel<Notification> getNotifications(@PathVariable String email) {
        List<Notification> notifications = notificationService.getNotificationsForRecipient(email);
        return CollectionModel.of(notifications,
                linkTo(methodOn(NotificationController.class).getNotifications(email)).withSelfRel());
    }
}
