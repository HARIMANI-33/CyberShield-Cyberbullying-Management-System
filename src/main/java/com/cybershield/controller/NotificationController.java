package com.cybershield.controller;

import com.cybershield.dto.NotificationResponse;
import com.cybershield.model.Notification;
import com.cybershield.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getNotifications(HttpSession session,
                                              @RequestParam(required = false) Long userId) {
        Long targetUserId = (Long) session.getAttribute("LOGGED_IN_USER_ID");
        if (targetUserId == null && userId != null) {
            targetUserId = userId;
        }

        if (targetUserId == null) {
            return ResponseEntity.ok(List.of());
        }

        List<Notification> list = notificationService.getUserNotifications(targetUserId);
        List<NotificationResponse> responses = list.stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getType(),
                        n.getComplaintNumber(),
                        n.isRead(),
                        n.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpSession session,
                                           @RequestParam(required = false) Long userId) {
        Long targetUserId = (Long) session.getAttribute("LOGGED_IN_USER_ID");
        if (targetUserId == null && userId != null) {
            targetUserId = userId;
        }

        if (targetUserId != null) {
            notificationService.markAllAsRead(targetUserId);
        }
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }
}
