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

    public EvidenceService(EvidenceRepository evidenceRepository,
                           ComplaintRepository complaintRepository) {
        this.evidenceRepository = evidenceRepository;
        this.complaintRepository = complaintRepository;
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
        if (status == null || (!status.equalsIgnoreCase("VERIFIED") && !status.equalsIgnoreCase("REJECTED"))) {
            throw new IllegalArgumentException("Invalid verification status. Allowed values: VERIFIED, REJECTED");
        }

        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new IllegalArgumentException("Evidence not found with ID: " + evidenceId));

        evidence.setVerificationStatus(status.toUpperCase());
        Evidence updated = evidenceRepository.save(evidence);

        return new EvidenceResponse(
                updated.getId(),
                "Evidence verification status updated to " + updated.getVerificationStatus(),
                updated.getFileName(),
                updated.getVerificationStatus(),
                updated.getUploadedAt()
        );
    }
}
