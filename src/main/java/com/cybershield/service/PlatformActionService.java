package com.cybershield.service;

import com.cybershield.config.PlatformContactConfig;
import com.cybershield.dto.GeneratePlatformActionRequest;
import com.cybershield.dto.PlatformActionResponse;
import com.cybershield.dto.UpdatePlatformActionRequest;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.model.Evidence;
import com.cybershield.model.PlatformAction;
import com.cybershield.repository.ComplaintAnalysisRepository;
import com.cybershield.repository.ComplaintRepository;
import com.cybershield.repository.EvidenceRepository;
import com.cybershield.repository.PlatformActionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlatformActionService {

    private final PlatformActionRepository platformActionRepository;
    private final ComplaintRepository complaintRepository;
    private final EvidenceRepository evidenceRepository;
    private final ComplaintAnalysisRepository analysisRepository;
    private final PlatformContactConfig contactConfig;
    private final NotificationService notificationService;

    public PlatformActionService(PlatformActionRepository platformActionRepository,
                                 ComplaintRepository complaintRepository,
                                 EvidenceRepository evidenceRepository,
                                 ComplaintAnalysisRepository analysisRepository,
                                 PlatformContactConfig contactConfig,
                                 NotificationService notificationService) {
        this.platformActionRepository = platformActionRepository;
        this.complaintRepository = complaintRepository;
        this.evidenceRepository = evidenceRepository;
        this.analysisRepository = analysisRepository;
        this.contactConfig = contactConfig;
        this.notificationService = notificationService;
    }

    @Transactional
    public PlatformActionResponse generatePlatformAction(GeneratePlatformActionRequest request) {
        if (request == null || request.getComplaintId() == null) {
            throw new IllegalArgumentException("Complaint ID is required to generate platform action.");
        }

        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + request.getComplaintId()));

        String platform = (request.getPlatform() != null && !request.getPlatform().isBlank())
                ? request.getPlatform().trim()
                : (complaint.getPlatform() != null && !complaint.getPlatform().isBlank() ? complaint.getPlatform() : "Other");

        String recipient = (request.getRecipient() != null && !request.getRecipient().isBlank())
                ? request.getRecipient().trim()
                : contactConfig.getContactForPlatform(platform);

        String reportedAccount = (request.getReportedAccount() != null && !request.getReportedAccount().isBlank())
                ? request.getReportedAccount().trim()
                : "Identified in incident description / supporting evidence";

        String officerName = (request.getAdminOfficerName() != null && !request.getAdminOfficerName().isBlank())
                ? request.getAdminOfficerName().trim()
                : "Inspector K. Raman (CYBER-OFFICER-742)";

        // Construct individualized evidence information
        List<Evidence> evidenceList = evidenceRepository.findByComplaintId(complaint.getId());
        String evidenceInfo;
        if (evidenceList == null || evidenceList.isEmpty()) {
            evidenceInfo = "Complainant testimony provided. No digital screenshot uploaded.";
        } else {
            evidenceInfo = evidenceList.stream()
                    .map(e -> String.format("- %s [Status: %s]", e.getFileName(), e.getVerificationStatus()))
                    .collect(Collectors.joining("\n"));
        }

        // Fetch preliminary analysis risk score if available
        Optional<ComplaintAnalysis> analysisOpt = analysisRepository.findByComplaintId(complaint.getId());
        String severityStr = complaint.getThreatLevel() != null ? complaint.getThreatLevel().toUpperCase() : "MEDIUM";
        if (analysisOpt.isPresent()) {
            severityStr += " (Automated Risk Score: " + analysisOpt.get().getRiskScore() + "/10, Priority: " + analysisOpt.get().getPriority() + ")";
        }

        String adminReviewText = (complaint.getAdminRemarks() != null && !complaint.getAdminRemarks().isBlank())
                ? complaint.getAdminRemarks().trim()
                : "Case reviewed and prioritized by CyberShield cyber investigation officer.";

        // Unique Subject per complaint
        String subject = "Cyberbullying Report — " + complaint.getComplaintNumber();

        // Build Common Email Template using specific complaint data
        StringBuilder body = new StringBuilder();
        body.append("Dear Platform Safety Team,\n\n");
        body.append("CyberShield is submitting a cyberbullying report associated with:\n\n");
        body.append("Complaint ID: ").append(complaint.getComplaintNumber()).append("\n");
        body.append("Platform: ").append(platform).append("\n");
        body.append("Reported Account: ").append(reportedAccount).append("\n");
        body.append("Severity: ").append(severityStr).append("\n\n");
        body.append("Incident Description:\n").append(complaint.getDescription() != null ? complaint.getDescription().trim() : "N/A").append("\n\n");
        body.append("Evidence:\n").append(evidenceInfo).append("\n\n");
        body.append("Admin Review:\n").append(adminReviewText).append("\n\n");
        body.append("The complaint and supporting evidence have been reviewed by the CyberShield administration team.\n\n");
        body.append("We request that your safety team review this report and take appropriate action according to your platform policies.\n\n");
        body.append("Regards,\nCyberShield Cyber Safety Team\nLiaison: ").append(officerName);

        PlatformAction action = new PlatformAction();
        action.setComplaint(complaint);
        action.setComplaintNumber(complaint.getComplaintNumber());
        action.setPlatform(platform);
        action.setRecipient(recipient);
        action.setReportedAccount(reportedAccount);
        action.setSubject(subject);
        action.setMessage(body.toString());
        action.setActionStatus("DRAFT");
        action.setAdminOfficerName(officerName);

        PlatformAction saved = platformActionRepository.save(action);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PlatformActionResponse getPlatformActionById(Long id) {
        PlatformAction action = platformActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Platform action not found with ID: " + id));
        return mapToResponse(action);
    }

    @Transactional
    public PlatformActionResponse updatePlatformAction(Long id, UpdatePlatformActionRequest request) {
        PlatformAction action = platformActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Platform action not found with ID: " + id));

        if (request.getRecipient() != null && !request.getRecipient().isBlank()) {
            action.setRecipient(request.getRecipient().trim());
        }
        if (request.getReportedAccount() != null) {
            action.setReportedAccount(request.getReportedAccount().trim());
        }
        if (request.getSubject() != null && !request.getSubject().isBlank()) {
            action.setSubject(request.getSubject().trim());
        }
        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            action.setMessage(request.getMessage().trim());
        }
        if (request.getAdminNotes() != null) {
            action.setAdminNotes(request.getAdminNotes().trim());
        }

        action.setActionStatus("READY");
        PlatformAction updated = platformActionRepository.save(action);
        return mapToResponse(updated);
    }

    @Transactional
    public PlatformActionResponse sendPlatformAction(Long id) {
        PlatformAction action = platformActionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Platform action not found with ID: " + id));

        action.setActionStatus("SENT");
        action.setSentAt(LocalDateTime.now());
        PlatformAction updated = platformActionRepository.save(action);

        // Update the associated complaint status to ACTION_TAKEN
        Complaint complaint = action.getComplaint();
        if (complaint != null) {
            complaint.setStatus("ACTION_TAKEN");
            String dispatchNote = String.format("[Platform Action Dispatched] Formal safety notice sent to %s (%s) on %s. Ref: PA-%d.",
                    action.getPlatform(),
                    action.getRecipient(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    action.getId());

            String existingRemarks = complaint.getAdminRemarks();
            if (existingRemarks != null && !existingRemarks.isBlank()) {
                complaint.setAdminRemarks(existingRemarks + "\n" + dispatchNote);
            } else {
                complaint.setAdminRemarks(dispatchNote);
            }
            complaintRepository.save(complaint);

            // Notify complainant
            if (complaint.getUser() != null) {
                notificationService.notifyUser(
                        complaint.getUser(),
                        "Platform Escalation Dispatched: " + complaint.getComplaintNumber(),
                        String.format("CyberShield submitted an official report to the %s Trust & Safety team (%s).",
                                action.getPlatform(), action.getRecipient()),
                        "ACTION_TAKEN",
                        complaint.getComplaintNumber()
                );
            }
        }

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<PlatformActionResponse> getActionsForComplaint(Long complaintId) {
        return platformActionRepository.findByComplaintIdOrderByCreatedAtDesc(complaintId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PlatformActionResponse> getAllPlatformActions() {
        return platformActionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PlatformActionResponse mapToResponse(PlatformAction a) {
        return new PlatformActionResponse(
                a.getId(),
                a.getComplaint() != null ? a.getComplaint().getId() : null,
                a.getComplaintNumber(),
                a.getPlatform(),
                a.getRecipient(),
                a.getReportedAccount(),
                a.getSubject(),
                a.getMessage(),
                a.getActionStatus(),
                a.getAdminOfficerName(),
                a.getAdminNotes(),
                a.getCreatedAt(),
                a.getSentAt()
        );
    }
}
