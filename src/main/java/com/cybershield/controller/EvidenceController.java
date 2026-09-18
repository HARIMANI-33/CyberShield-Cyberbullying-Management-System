package com.cybershield.controller;

import com.cybershield.dto.EvidenceResponse;
import com.cybershield.dto.VerifyEvidenceRequest;
import com.cybershield.service.EvidenceService;
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

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyEvidence(@PathVariable Long id,
                                            @RequestBody VerifyEvidenceRequest request) {
        try {
            EvidenceResponse response = evidenceService.verifyEvidence(id, request.getVerificationStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
