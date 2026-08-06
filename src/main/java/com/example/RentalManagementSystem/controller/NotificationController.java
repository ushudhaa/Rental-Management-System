package com.example.RentalManagementSystem.controller;

import com.example.RentalManagementSystem.dto.NotificationResponse;
import com.example.RentalManagementSystem.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recent")
    public ResponseEntity<List<NotificationResponse>> recent(Principal principal) {
        return ResponseEntity.ok(notificationService.getRecent(principal.getName(), 8));
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> all(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(notificationService.getAll(principal.getName(), pageable));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Principal principal) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(principal.getName())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(Principal principal) {
        notificationService.markAllAsRead(principal.getName());
        return ResponseEntity.noContent().build();
    }
}