package com.example.project.service;

import java.util.List;

import com.example.project.dto.response.NotificationResponse;

public interface NotificationService {
    // Payments waiting on this user, most urgent first.
    List<NotificationResponse> getNotifications(Long userID);
}
