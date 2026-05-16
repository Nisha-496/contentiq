package com.contentiq.contentiq.service;

import com.contentiq.contentiq.model.Notification;
import com.contentiq.contentiq.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getForUser(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadForUser(String userId) {
        return notificationRepository.findByUserIdAndReadFalse(userId);
    }

    public Notification markAsRead(String notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        n.setRead(true);
        return notificationRepository.save(n);
    }
}
