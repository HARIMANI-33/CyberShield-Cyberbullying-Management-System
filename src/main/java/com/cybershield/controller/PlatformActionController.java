package com.cybershield.controller;

import com.cybershield.config.PlatformContactConfig;
import com.cybershield.dto.GeneratePlatformActionRequest;
import com.cybershield.dto.PlatformActionResponse;
import com.cybershield.dto.UpdatePlatformActionRequest;
import com.cybershield.model.User;
import com.cybershield.service.PlatformActionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform-actions")
public class PlatformActionController {

    private final PlatformActionService platformActionService;
    private final PlatformContactConfig platformContactConfig;

    public PlatformActionController(PlatformActionService platformActionService,
                                    PlatformContactConfig platformContactConfig) {
        this.platformActionService = platformActionService;
        this.platformContactConfig = platformContactConfig;
    }

    private boolean isForbiddenUser(HttpSession session) {
        if (session == null) return false;
        String role = (String) session.getAttribute(UserController.SESSION_USER_ROLE);
        Long userId = (Long) session.getAttribute(UserController.SESSION_USER_ID);
        if (userId != null) {
            return role == null || (!role.equalsIgnoreCase("ADMIN") && !role.equalsIgnoreCase("OFFICER") && !role.equalsIgnoreCase("ADMIN_OFFICER"));
        }
        return false;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlatformAction(@RequestBody GeneratePlatformActionRequest request,
                                                    HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Platform reporting is restricted to authorized cyber crime officers."));
        }
        try {
            PlatformActionResponse response = platformActionService.generatePlatformAction(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlatformActionById(@PathVariable Long id, HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Unauthorized access."));
        }
        try {
            PlatformActionResponse response = platformActionService.getPlatformActionById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlatformAction(@PathVariable Long id,
                                                  @RequestBody UpdatePlatformActionRequest request,
                                                  HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Unauthorized access."));
        }
        try {
            PlatformActionResponse response = platformActionService.updatePlatformAction(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<?> sendPlatformAction(@PathVariable Long id, HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Only administrators may confirm platform escalations."));
        }
        try {
            PlatformActionResponse response = platformActionService.sendPlatformAction(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/complaint/{complaintId}")
    public ResponseEntity<?> getActionsForComplaint(@PathVariable Long complaintId, HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Unauthorized access."));
        }
        List<PlatformActionResponse> actions = platformActionService.getActionsForComplaint(complaintId);
        return ResponseEntity.ok(actions);
    }

    @GetMapping
    public ResponseEntity<?> getAllPlatformActions(HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Unauthorized access."));
        }
        List<PlatformActionResponse> actions = platformActionService.getAllPlatformActions();
        return ResponseEntity.ok(actions);
    }

    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts() {
        return ResponseEntity.ok(platformContactConfig.getAllContacts());
    }

    @PutMapping("/contacts")
    public ResponseEntity<?> updateContact(@RequestBody Map<String, String> payload, HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Unauthorized access."));
        }
        String platform = payload.get("platform");
        String email = payload.get("email");
        if (platform == null || email == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Platform and email are required."));
        }
        platformContactConfig.updateContact(platform, email);
        return ResponseEntity.ok(Map.of("message", "Contact updated successfully", "contacts", platformContactConfig.getAllContacts()));
    }
}
