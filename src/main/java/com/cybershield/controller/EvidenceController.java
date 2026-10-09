package com.cybershield.controller;

import com.cybershield.dto.EvidenceResponse;
import com.cybershield.dto.VerifyEvidenceRequest;
import com.cybershield.model.User;
import com.cybershield.service.EvidenceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
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

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyEvidence(@PathVariable Long id,
                                            @RequestBody VerifyEvidenceRequest request,
                                            HttpSession session) {
        if (isForbiddenUser(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied: Evidence verification is restricted to cyber crime officers."));
        }
        try {
            EvidenceResponse response;
            if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
                response = evidenceService.verifyEvidence(id, request.getVerificationStatus(), request.getRemarks());
            } else {
                response = evidenceService.verifyEvidence(id, request.getVerificationStatus());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
