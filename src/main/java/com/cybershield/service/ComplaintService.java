package com.cybershield.service;

import com.cybershield.dto.ComplaintDetailResponse;
import com.cybershield.dto.ComplaintResponse;
import com.cybershield.dto.ComplaintSummaryResponse;
import com.cybershield.dto.CreateComplaintRequest;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.model.Evidence;
import com.cybershield.model.User;
import com.cybershield.repository.ComplaintAnalysisRepository;
import com.cybershield.repository.ComplaintRepository;
import com.cybershield.repository.EvidenceRepository;
import com.cybershield.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "PENDING",
            "REVIEWED",
            "ACCEPTED",
            "SUBMITTED",
            "UNDER_REVIEW",
            "EVIDENCE_VERIFICATION",
            "EVIDENCE_VERIFIED",
            "INVESTIGATION",
            "INVESTIGATING",
            "ACTION_TAKEN",
            "RESOLVED",
            "REJECTED",
            "NEEDS_MORE_INFO"
    );

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final SeverityAnalysisService severityAnalysisService;
    private final EvidenceRepository evidenceRepository;
    private final ComplaintAnalysisRepository complaintAnalysisRepository;
    private final NotificationService notificationService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            SeverityAnalysisService severityAnalysisService,
                            EvidenceRepository evidenceRepository,
                            ComplaintAnalysisRepository complaintAnalysisRepository,
                            NotificationService notificationService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.severityAnalysisService = severityAnalysisService;
        this.evidenceRepository = evidenceRepository;
        this.complaintAnalysisRepository = complaintAnalysisRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public ComplaintResponse registerComplaint(CreateComplaintRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : "Cyberbullying Report");
        complaint.setCategory(request.getCategory() != null ? request.getCategory() : "Online Harassment");
        complaint.setPlatform(request.getPlatform() != null ? request.getPlatform() : "Other");
        complaint.setDescription(request.getDescription());
        complaint.setIncidentDateTime(request.getIncidentDateTime());
        complaint.setUser(user);
        complaint.setStatus("PENDING");

        // Save initial complaint
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Perform automatic severity analysis
        ComplaintAnalysis analysis = severityAnalysisService.analyzeComplaint(savedComplaint);

        // Save updated complaint threatLevel and priority
        complaintRepository.save(savedComplaint);

        // Trigger real notification for user
        notificationService.notifyUser(
                user,
                "Complaint Filed: " + savedComplaint.getComplaintNumber(),
                "Incident on " + savedComplaint.getPlatform() + " filed. Automated triage assessed preliminary severity: " + analysis.getDetectedSeverity() + ".",
                "SUBMISSION",
                savedComplaint.getComplaintNumber()
        );

        return new ComplaintResponse(
                savedComplaint.getId(),
                savedComplaint.getComplaintNumber(),
                "Complaint registered successfully",
                analysis.getDetectedSeverity(),
                analysis.getRiskScore(),
                analysis.getPriority(),
                savedComplaint.getStatus(),
                analysis.getAssessmentNote(),
                analysis.getDetectedCategory(),
                analysis.getMatchedTerms()
        );
    }

    @Transactional(readOnly = true)
    public ComplaintDetailResponse getComplaintDetails(Long id) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + id));

        List<Evidence> evidenceList = evidenceRepository.findByComplaintId(id);
        List<ComplaintDetailResponse.EvidenceItem> evidenceItems = evidenceList.stream()
                .map(e -> new ComplaintDetailResponse.EvidenceItem(
                        e.getId(),
                        e.getFileName(),
                        e.getFilePath(),
                        e.getVerificationStatus(),
                        e.getUploadedAt()
                ))
                .collect(Collectors.toList());

        ComplaintDetailResponse.PreliminaryAnalysisInfo analysisInfo = complaintAnalysisRepository.findByComplaintId(id)
                .map(a -> new ComplaintDetailResponse.PreliminaryAnalysisInfo(
                        a.getRiskScore(),
                        a.getDetectedSeverity(),
                        a.getPriority(),
                        a.getMatchedCount(),
                        a.getAnalysisDate()
                ))
                .orElse(null);

        ComplaintDetailResponse response = new ComplaintDetailResponse();
        response.setId(complaint.getId());
        response.setComplaintNumber(complaint.getComplaintNumber());
        response.setTitle(complaint.getTitle());
        response.setCategory(complaint.getCategory());
        response.setPlatform(complaint.getPlatform());
        response.setDescription(complaint.getDescription());
        response.setThreatLevel(complaint.getThreatLevel());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setIncidentDateTime(complaint.getIncidentDateTime());
        response.setAdminRemarks(complaint.getAdminRemarks());

        if (complaint.getUser() != null) {
            response.setUserId(complaint.getUser().getId());
            response.setUserName(complaint.getUser().getName());
            response.setUserEmail(complaint.getUser().getEmail());
        }

        response.setEvidence(evidenceItems);
        response.setPreliminaryAnalysis(analysisInfo);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return complaints.stream()
                .map(c -> new ComplaintSummaryResponse(
                        c.getId(),
                        c.getComplaintNumber(),
                        c.getTitle(),
                        (c.getUser() != null) ? c.getUser().getName() : "Unknown",
                        c.getThreatLevel(),
                        c.getPriority(),
                        c.getStatus(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getComplaintsByUserId(Long userId) {
        List<Complaint> complaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return complaints.stream()
                .map(c -> new ComplaintSummaryResponse(
                        c.getId(),
                        c.getComplaintNumber(),
                        c.getTitle(),
                        (c.getUser() != null) ? c.getUser().getName() : "Unknown",
                        c.getThreatLevel(),
                        c.getPriority(),
                        c.getStatus(),
                        c.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isComplaintOwnedByUser(Long complaintId, Long userId) {
        return complaintRepository.findById(complaintId)
                .map(c -> c.getUser() != null && c.getUser().getId().equals(userId))
                .orElse(false);
    }

    @Transactional
    public ComplaintDetailResponse updateStatus(Long id, String newStatus, String adminRemarks) {
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Allowed: " + ALLOWED_STATUSES);
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + id));

        complaint.setStatus(newStatus.toUpperCase());
        if (adminRemarks != null) {
            complaint.setAdminRemarks(adminRemarks);
        }
        complaintRepository.save(complaint);

        if (complaint.getUser() != null) {
            String title;
            String message;
            String type = "STATUS_CHANGE";

            if ("REVIEWED".equalsIgnoreCase(complaint.getStatus())) {
                title = "🔍 Complaint Reviewed";
                message = "Your complaint " + complaint.getComplaintNumber() + " has been reviewed by the CyberShield team.\nStatus: REVIEWED";
                type = "REVIEWED";
            } else if ("ACCEPTED".equalsIgnoreCase(complaint.getStatus())) {
                title = "✅ Complaint Accepted";
                message = "Your complaint " + complaint.getComplaintNumber() + " has been accepted for further investigation.\nStatus: ACCEPTED";
                type = "ACCEPTED";
            } else {
                String remarksInfo = (adminRemarks != null && !adminRemarks.isBlank()) ? " Officer note: " + adminRemarks : "";
                title = "Case Status Updated: " + complaint.getComplaintNumber();
                message = "Your complaint has progressed to " + complaint.getStatus() + "." + remarksInfo;
                type = complaint.getStatus();
            }

            notificationService.notifyUser(
                    complaint.getUser(),
                    title,
                    message,
                    type,
                    complaint.getComplaintNumber()
            );
        }

        return getComplaintDetails(id);
    }

    @Transactional
    public ComplaintDetailResponse updateStatus(Long id, String newStatus) {
        return updateStatus(id, newStatus, null);
    }

    private synchronized String generateComplaintNumber() {
        int currentYear = LocalDate.now().getYear();
        long count = complaintRepository.count() + 1;
        String candidate = String.format("CS-%d-%03d", currentYear, count);
        while (complaintRepository.existsByComplaintNumber(candidate)) {
            count++;
            candidate = String.format("CS-%d-%03d", currentYear, count);
        }
        return candidate;
    }
}
