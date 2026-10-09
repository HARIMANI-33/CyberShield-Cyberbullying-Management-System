package com.cybershield.controller;

import com.cybershield.dto.LoginRequest;
import com.cybershield.dto.RegisterUserRequest;
import com.cybershield.dto.UserProfileResponse;
import com.cybershield.dto.UserResponse;
import com.cybershield.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    public static final String SESSION_USER_ID = "LOGGED_IN_USER_ID";
    public static final String SESSION_USER_EMAIL = "LOGGED_IN_USER_EMAIL";
    public static final String SESSION_USER_ROLE = "LOGGED_IN_USER_ROLE";
    public static final String SESSION_USER_OBJ = "LOGGED_IN_USER";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterUserRequest request, HttpSession session) {
        try {
            UserResponse response = userService.registerUser(request);
            if (session != null) {
                session.setAttribute(SESSION_USER_ID, response.getId());
                session.setAttribute(SESSION_USER_EMAIL, response.getEmail());
                session.setAttribute(SESSION_USER_ROLE, response.getRole());
                session.setAttribute(SESSION_USER_OBJ, response);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request, HttpSession session) {
        try {
            UserResponse response = userService.loginUser(request.getEmail(), request.getPassword());
            if (session != null) {
                session.setAttribute(SESSION_USER_ID, response.getId());
                session.setAttribute(SESSION_USER_EMAIL, response.getEmail());
                session.setAttribute(SESSION_USER_ROLE, response.getRole());
                session.setAttribute(SESSION_USER_OBJ, response);
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        if (session == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "No active session"));
        }
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Not logged in"));
        }
        try {
            UserResponse user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpSession session,
                                            @RequestParam(required = false) Long userId) {
        Long targetUserId = (Long) session.getAttribute(SESSION_USER_ID);
        if (targetUserId == null && userId != null) {
            targetUserId = userId;
        }

        if (targetUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Authentication required to view profile"));
        }

        try {
            UserProfileResponse profile = userService.getUserProfile(targetUserId);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }
}
