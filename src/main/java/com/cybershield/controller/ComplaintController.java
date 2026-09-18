package com.cybershield.controller;

import com.cybershield.dto.*;
import com.cybershield.service.ComplaintService;
import com.cybershield.service.EvidenceService;
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
    public ResponseEntity<?> createComplaint(@RequestBody CreateComplaintRequest request) {
        try {
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
    public ResponseEntity<?> getComplaintById(@PathVariable Long id) {
        try {
            ComplaintDetailResponse response = complaintService.getComplaintDetails(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ComplaintSummaryResponse>> getAllComplaints() {
        List<ComplaintSummaryResponse> complaints = complaintService.getAllComplaints();
        return ResponseEntity.ok(complaints);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateComplaintStatus(@PathVariable Long id,
                                                   @RequestBody UpdateStatusRequest request) {
        try {
            ComplaintDetailResponse response = complaintService.updateStatus(id, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
