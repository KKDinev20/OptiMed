package com.optimednotifications.controller;

import com.optimednotifications.entity.*;
import com.optimednotifications.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postRequestToRegisterEndpoint_shouldRegisterRecipient() throws Exception {
        mockMvc.perform(post("/api/notifications/register")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).ensureRecipientExists("test@example.com");
    }

    @Test
    void postRequestToSendNotification_shouldCreateNotification() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setEmail("test@example.com");

        Notification mockNotification = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(recipient)
                .message("Your appointment is confirmed.")
                .createdAt(LocalDateTime.now())
                .isReceived(false)
                .build();

        when(notificationService.createNotification("test@example.com", "Your appointment is confirmed."))
                .thenReturn(mockNotification);

        ResultActions result = mockMvc.perform(post("/api/notifications")
                .param("email", "test@example.com")
                .param("message", "Your appointment is confirmed.")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.recipient.email").value("test@example.com"))
                .andExpect(jsonPath("$.message").value("Your appointment is confirmed."));

        verify(notificationService, times(1)).createNotification("test@example.com", "Your appointment is confirmed.");
    }


    @Test
    void getRequestToRetrieveNotifications_shouldReturnList() throws Exception {
        Recipient recipient = new Recipient();
        recipient.setEmail("test@example.com");
        List<Notification> notifications = List.of(
                Notification.builder()
                        .id(UUID.randomUUID())
                        .recipient(recipient)
                        .message("Reminder: Check-up tomorrow.")
                        .createdAt(LocalDateTime.now())
                        .isReceived(false)
                        .build(),
                Notification.builder()
                        .id(UUID.randomUUID())
                        .recipient(recipient)
                        .message("Lab results are available.")
                        .createdAt(LocalDateTime.now())
                        .isReceived(false)
                        .build()
        );

        when(notificationService.getNotificationsForRecipient("test@example.com"))
                .thenReturn(notifications);

        ResultActions result = mockMvc.perform(get("/api/notifications/email/test@example.com")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificationList[0].message").value("Reminder: Check-up tomorrow."))
                .andExpect(jsonPath("$._embedded.notificationList[1].message").value("Lab results are available."));

        verify(notificationService, times(1)).getNotificationsForRecipient("test@example.com");
    }

}
