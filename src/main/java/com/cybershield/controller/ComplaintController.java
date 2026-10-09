package com.cybershield.controller;

import com.cybershield.dto.*;
import com.cybershield.service.ComplaintService;
import com.cybershield.service.EvidenceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final EvidenceService evidenceService;

    public ComplaintController(ComplaintService complaintService,
                               EvidenceService evidenceService) {
        this.complaintService = complaintService;
        this.evidenceService = evidenceService;
    }

    @PostMapping
    public ResponseEntity<?> createComplaint(@RequestBody CreateComplaintRequest request, HttpSession session) {
        try {
            // Auto-detect logged-in user if not provided in payload
            if (request.getUserId() == null && session != null) {
                Long sessionUserId = (Long) session.getAttribute(UserController.SESSION_USER_ID);
                if (sessionUserId != null) {
                    request.setUserId(sessionUserId);
                }
            }

            if (request.getUserId() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "You must be logged in to submit an incident report"));
            }

            ComplaintResponse response = complaintService.registerComplaint(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping(value = "/{complaintId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadEvidence(@PathVariable Long complaintId,
                                            @RequestParam("file") MultipartFile file) {
        try {
            EvidenceResponse response = evidenceService.uploadEvidence(complaintId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "File upload failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getComplaintById(@PathVariable Long id, HttpSession session) {
        try {
            // Security verification: prevent cross-user unauthorized access
            if (session != null) {
                Long sessionUserId = (Long) session.getAttribute(UserController.SESSION_USER_ID);
                String role = (String) session.getAttribute(UserController.SESSION_USER_ROLE);
                if (sessionUserId != null && !"ADMIN".equalsIgnoreCase(role) && !"ADMIN_OFFICER".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                    if (!complaintService.isComplaintOwnedByUser(id, sessionUserId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: You do not have permission to view this complaint."));
                    }
                }
            }

            ComplaintDetailResponse response = complaintService.getComplaintDetails(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyComplaints(HttpSession session) {
        Long sessionUserId = (session != null) ? (Long) session.getAttribute(UserController.SESSION_USER_ID) : null;
        if (sessionUserId == null) {
            return ResponseEntity.ok(List.of());
        }
        List<ComplaintSummaryResponse> complaints = complaintService.getComplaintsByUserId(sessionUserId);
        return ResponseEntity.ok(complaints);
    }

    @GetMapping
    public ResponseEntity<List<ComplaintSummaryResponse>> getAllComplaints() {
        List<ComplaintSummaryResponse> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getComplaintsByUserId(@PathVariable Long userId, HttpSession session) {
        if (session != null) {
            Long sessionUserId = (Long) session.getAttribute(UserController.SESSION_USER_ID);
            String role = (String) session.getAttribute(UserController.SESSION_USER_ROLE);
            if (sessionUserId != null && !"ADMIN".equalsIgnoreCase(role) && !"ADMIN_OFFICER".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                if (!sessionUserId.equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Cannot access another user's complaints."));
                }
            }
        }
        List<ComplaintSummaryResponse> complaints = complaintService.getComplaintsByUserId(userId);
        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(@PathVariable Long id,
                                                   @RequestBody UpdateStatusRequest request,
                                                   HttpSession session) {
        if (session != null) {
            Long sessionUserId = (Long) session.getAttribute(UserController.SESSION_USER_ID);
            String role = (String) session.getAttribute(UserController.SESSION_USER_ROLE);
            if (sessionUserId != null && !"ADMIN".equalsIgnoreCase(role) && !"ADMIN_OFFICER".equalsIgnoreCase(role) && !"OFFICER".equalsIgnoreCase(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied: Status updates are restricted to officers."));
            }
        }
        try {
            ComplaintDetailResponse response;
            if (request.getAdminRemarks() != null && !request.getAdminRemarks().isBlank()) {
                response = complaintService.updateStatus(id, request.getStatus(), request.getAdminRemarks());
            } else {
                response = complaintService.updateStatus(id, request.getStatus());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
