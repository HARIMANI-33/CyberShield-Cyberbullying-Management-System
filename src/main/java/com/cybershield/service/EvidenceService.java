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
import java.util.ArrayList;
import java.util.List;
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

    public static void validateScreenshotFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File " + (file.getOriginalFilename() != null ? file.getOriginalFilename() : "") + " exceeds the 10MB limit");
        }
        String originalFilename = file.getOriginalFilename();
        String filenameLower = (originalFilename != null) ? originalFilename.toLowerCase().trim() : "";
        if (!(filenameLower.endsWith(".png") || filenameLower.endsWith(".jpg") || filenameLower.endsWith(".jpeg") || filenameLower.endsWith(".webp"))) {
            throw new IllegalArgumentException("Invalid file format. Only PNG, JPG, JPEG, and WebP files are allowed.");
        }
    }

    @Transactional
    public Evidence saveEvidenceForComplaint(Complaint complaint, MultipartFile file) throws IOException {
        validateScreenshotFile(file);

        if (!Files.exists(UPLOAD_PATH)) {
            Files.createDirectories(UPLOAD_PATH);
        }

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "screenshot.png";
        String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        Path targetLocation = UPLOAD_PATH.resolve(savedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        Evidence evidence = new Evidence();
        evidence.setComplaint(complaint);
        evidence.setFileName(originalFilename);
        evidence.setFilePath("/uploads/evidence/" + savedFilename);
        evidence.setVerificationStatus("PENDING");

        return evidenceRepository.save(evidence);
    }

    @Transactional
    public EvidenceResponse uploadEvidence(Long complaintId, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + complaintId));

        Evidence saved = saveEvidenceForComplaint(complaint, file);

        if (complaint.getUser() != null) {
            notificationService.notifyUser(
                    complaint.getUser(),
                    "Evidence Received: " + complaint.getComplaintNumber(),
                    "Screenshot proof \"" + saved.getFileName() + "\" has been attached and logged for officer review.",
                    "EVIDENCE_VERIFIED",
                    complaint.getComplaintNumber()
            );
        }

        EvidenceResponse response = new EvidenceResponse(
                saved.getId(),
                "Evidence uploaded successfully",
                saved.getFileName(),
                saved.getVerificationStatus(),
                saved.getUploadedAt()
        );
        response.setFilePath(saved.getFilePath());
        return response;
    }

    @Transactional
    public List<EvidenceResponse> uploadMultipleEvidence(Long complaintId, java.util.List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one screenshot evidence file is required");
        }

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + complaintId));

        long totalSize = 0;
        int validFileCount = 0;
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                validateScreenshotFile(f);
                totalSize += f.getSize();
                validFileCount++;
            }
        }

        if (validFileCount == 0) {
            throw new IllegalArgumentException("At least one screenshot evidence file is required");
        }

        if (totalSize > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Total upload size exceeds the 50MB limit");
        }

        java.util.List<EvidenceResponse> responses = new java.util.ArrayList<>();
        for (MultipartFile f : files) {
            if (f != null && !f.isEmpty()) {
                Evidence saved = saveEvidenceForComplaint(complaint, f);
                EvidenceResponse resp = new EvidenceResponse(
                        saved.getId(),
                        "Evidence uploaded successfully",
                        saved.getFileName(),
                        saved.getVerificationStatus(),
                        saved.getUploadedAt()
                );
                resp.setFilePath(saved.getFilePath());
                responses.add(resp);
            }
        }

        if (complaint.getUser() != null) {
            notificationService.notifyUser(
                    complaint.getUser(),
                    "Evidence Received: " + complaint.getComplaintNumber(),
                    responses.size() + " screenshot proof(s) attached and logged for officer review.",
                    "EVIDENCE_VERIFIED",
                    complaint.getComplaintNumber()
            );
        }

        return responses;
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
