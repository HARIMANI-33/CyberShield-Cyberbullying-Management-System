package com.cybershield.service;

import com.cybershield.dto.EvidenceResponse;
import com.cybershield.model.Complaint;
import com.cybershield.model.Evidence;
import com.cybershield.repository.ComplaintRepository;
import com.cybershield.repository.EvidenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class EvidenceService {

    private static final Path UPLOAD_PATH = Paths.get("uploads", "evidence");

    private final EvidenceRepository evidenceRepository;
    private final ComplaintRepository complaintRepository;
    private final NotificationService notificationService;

    public EvidenceService(EvidenceRepository evidenceRepository,
                           ComplaintRepository complaintRepository,
                           NotificationService notificationService) {
        this.evidenceRepository = evidenceRepository;
        this.complaintRepository = complaintRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public EvidenceResponse uploadEvidence(Long complaintId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String filenameLower = (originalFilename != null) ? originalFilename.toLowerCase() : "";

        if (!(filenameLower.endsWith(".png") || filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg"))) {
            throw new IllegalArgumentException("Invalid file format. Only PNG, JPG, and JPEG files are allowed.");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + complaintId));

        if (!Files.exists(UPLOAD_PATH)) {
            Files.createDirectories(UPLOAD_PATH);
        }

        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetLocation = UPLOAD_PATH.resolve(savedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        Evidence evidence = new Evidence();
        evidence.setComplaint(complaint);
        evidence.setFileName(originalFilename);
        evidence.setFilePath(targetLocation.toString());
        evidence.setVerificationStatus("PENDING");

        Evidence saved = evidenceRepository.save(evidence);

        if (complaint.getUser() != null) {
            notificationService.notifyUser(
                    complaint.getUser(),
                    "Evidence Received: " + complaint.getComplaintNumber(),
                    "Screenshot proof \"" + originalFilename + "\" has been attached and logged for officer review.",
                    "EVIDENCE_VERIFIED",
                    complaint.getComplaintNumber()
            );
        }

        return new EvidenceResponse(
                saved.getId(),
                "Evidence uploaded successfully",
                saved.getFileName(),
                saved.getVerificationStatus(),
                saved.getUploadedAt()
        );
    }

    @Transactional
    public EvidenceResponse verifyEvidence(Long evidenceId, String status) {
        return verifyEvidence(evidenceId, status, null);
    }

    @Transactional
    public EvidenceResponse verifyEvidence(Long evidenceId, String status, String remarks) {
        if (status == null || (!status.equalsIgnoreCase("VERIFIED") && !status.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Invalid verification status. Allowed values: VERIFIED, REJECTED");
        }

        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found with ID: " + evidenceId));

        evidence.setVerificationStatus(status.toUpperCase());
        Evidence updated = evidenceRepository.save(evidence);

        Complaint complaint = evidence.getComplaint();
        if (complaint != null) {
            // If verified, advance complaint status to EVIDENCE_VERIFIED if currently SUBMITTED or UNDER_REVIEW
            if (status.equalsIgnoreCase("VERIFIED")) {
                if ("SUBMITTED".equalsIgnoreCase(complaint.getStatus()) || "PENDING".equalsIgnoreCase(complaint.getStatus()) 
                        || "UNDER_REVIEW".equalsIgnoreCase(complaint.getStatus()) || "REVIEWED".equalsIgnoreCase(complaint.getStatus())) {
                    complaint.setStatus("EVIDENCE_VERIFIED");
                }
            }

            // Record remarks if provided
            if (remarks != null && !remarks.isBlank()) {
                String existing = complaint.getAdminRemarks();
                String note = String.format("[Evidence %s] %s", updated.getVerificationStatus(), remarks.trim());
                complaint.setAdminRemarks(existing != null && !existing.isBlank() ? (existing + "\n" + note) : note);
            }

            complaintRepository.save(complaint);

            if (complaint.getUser() != null) {
                String complaintNum = complaint.getComplaintNumber();
                String notifMsg = "Digital proof \"" + updated.getFileName() + "\" was marked " + updated.getVerificationStatus() + " by investigating officer.";
                if (remarks != null && !remarks.isBlank()) {
                    notifMsg += " Officer note: " + remarks.trim();
                }
                notificationService.notifyUser(
                        complaint.getUser(),
                        "Evidence " + updated.getVerificationStatus() + ": " + complaintNum,
                        notifMsg,
                        "EVIDENCE_VERIFIED",
                        complaintNum
                );
            }
        }

        return new EvidenceResponse(
                updated.getId(),
                "Evidence verification status updated to " + updated.getVerificationStatus(),
                updated.getFileName(),
                updated.getVerificationStatus(),
                updated.getUploadedAt()
        );
    }
}
