package com.example.RentalManagementSystem.dto;

import com.example.RentalManagementSystem.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String link;
    private NotificationType type;
    private boolean read;
    private LocalDateTime createdAt;
}