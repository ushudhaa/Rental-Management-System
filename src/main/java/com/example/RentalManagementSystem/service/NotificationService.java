package com.example.RentalManagementSystem.service;

import com.example.RentalManagementSystem.dto.NotificationResponse;
import com.example.RentalManagementSystem.entity.Notification;
import com.example.RentalManagementSystem.entity.User;
import com.example.RentalManagementSystem.enums.NotificationType;
import com.example.RentalManagementSystem.enums.Role;
import com.example.RentalManagementSystem.exception.ResourceNotFoundException;
import com.example.RentalManagementSystem.repository.NotificationRepository;
import com.example.RentalManagementSystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyUser(User recipient, String title, String message, String link, NotificationType type) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .link(link)
                .type(type)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void notifyRole(Role role, String title, String message, String link, NotificationType type) {
        for (User recipient : userRepository.findByRole(role)) {
            notifyUser(recipient, title, message, link, type);
        }
    }

    public List<NotificationResponse> getRecent(String email, int limit) {
        User user = findUser(email);
        return notificationRepository.findTop10ByRecipientIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    public Page<NotificationResponse> getAll(String email, Pageable pageable) {
        User user = findUser(email);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    public long getUnreadCount(String email) {
        User user = findUser(email);
        return notificationRepository.countByRecipientIdAndIsReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(Long id, String email) {
        User user = findUser(email);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String email) {
        User user = findUser(email);
        List<Notification> unread = notificationRepository.findByRecipientIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .link(n.getLink())
                .type(n.getType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}