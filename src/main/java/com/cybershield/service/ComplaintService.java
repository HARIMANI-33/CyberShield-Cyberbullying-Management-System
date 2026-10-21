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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final EvidenceService evidenceService;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            SeverityAnalysisService severityAnalysisService,
                            EvidenceRepository evidenceRepository,
                            ComplaintAnalysisRepository complaintAnalysisRepository,
                            NotificationService notificationService,
                            EvidenceService evidenceService) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.severityAnalysisService = severityAnalysisService;
        this.evidenceRepository = evidenceRepository;
        this.complaintAnalysisRepository = complaintAnalysisRepository;
        this.notificationService = notificationService;
        this.evidenceService = evidenceService;
    }

    public static boolean isValidHttpOrHttpsUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String trimmed = url.trim();
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            return false;
        }
        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null) return false;
            String schemeLower = scheme.toLowerCase();
            if (!schemeLower.equals("http") && !schemeLower.equals("https")) {
                return false;
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void validateComplaintFields(String title, String category, String platform, String description, String postUrl) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint title is required");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Incident category is required");
        }
        if (platform == null || platform.trim().isEmpty()) {
            throw new IllegalArgumentException("Platform is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint description is required and cannot be empty");
        }
        if (postUrl == null || postUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Post URL is required");
        }
        if (!isValidHttpOrHttpsUrl(postUrl)) {
            throw new IllegalArgumentException("Invalid Post URL. Must be a valid HTTP or HTTPS address");
        }
    }

    @Transactional
    public ComplaintResponse registerComplaint(CreateComplaintRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (request.getTitle() != null && request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint title is required");
        }
        if (request.getDescription() != null && request.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Complaint description is required and cannot be empty");
        }
        if (request.getPostUrl() != null && !request.getPostUrl().trim().isEmpty()) {
            if (!isValidHttpOrHttpsUrl(request.getPostUrl())) {
                throw new IllegalArgumentException("Invalid Post URL. Must be a valid HTTP or HTTPS address");
            }
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle().trim() : "Cyberbullying Report");
        complaint.setCategory(request.getCategory() != null && !request.getCategory().isBlank() ? request.getCategory().trim() : "Online Harassment");
        complaint.setPlatform(request.getPlatform() != null && !request.getPlatform().isBlank() ? request.getPlatform().trim() : "Other");
        complaint.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        if (request.getPostUrl() != null && !request.getPostUrl().trim().isEmpty()) {
            complaint.setPostUrl(request.getPostUrl().trim());
        }
        String repName = (request.getReporterName() != null && !request.getReporterName().isBlank())
                ? request.getReporterName().trim()
                : (user != null && user.getName() != null ? user.getName() : "Anonymous User");
        complaint.setReporterName(repName);
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

        ComplaintResponse response = new ComplaintResponse(
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
        response.setPostUrl(savedComplaint.getPostUrl());
        response.setReporterName(savedComplaint.getReporterName());
        return response;
    }

    @Transactional
    public ComplaintResponse registerComplaintWithEvidence(CreateComplaintRequest request, List<MultipartFile> files) throws IOException {
        validateComplaintFields(request.getTitle(), request.getCategory(), request.getPlatform(), request.getDescription(), request.getPostUrl());

        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("At least one screenshot evidence file is required");
        }

        List<MultipartFile> validFiles = files.stream()
                .filter(f -> f != null && !f.isEmpty())
                .collect(Collectors.toList());

        if (validFiles.isEmpty()) {
            throw new IllegalArgumentException("At least one screenshot evidence file is required");
        }

        long totalSize = 0;
        for (MultipartFile file : validFiles) {
            EvidenceService.validateScreenshotFile(file);
            totalSize += file.getSize();
        }
        if (totalSize > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Total upload size exceeds the 50MB limit");
        }

        if (request.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getUserId()));

        String complaintNumber = generateComplaintNumber();

        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(complaintNumber);
        complaint.setTitle(request.getTitle().trim());
        complaint.setCategory(request.getCategory().trim());
        complaint.setPlatform(request.getPlatform().trim());
        complaint.setDescription(request.getDescription().trim());
        complaint.setPostUrl(request.getPostUrl().trim());
        String repName = (request.getReporterName() != null && !request.getReporterName().isBlank())
                ? request.getReporterName().trim()
                : (user != null && user.getName() != null ? user.getName() : "Anonymous User");
        complaint.setReporterName(repName);
        complaint.setIncidentDateTime(request.getIncidentDateTime());
        complaint.setUser(user);
        complaint.setStatus("PENDING");

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Save each attached evidence file
        for (MultipartFile file : validFiles) {
            evidenceService.saveEvidenceForComplaint(savedComplaint, file);
        }

        // Automatic severity analysis
        ComplaintAnalysis analysis = severityAnalysisService.analyzeComplaint(savedComplaint);
        complaintRepository.save(savedComplaint);

        // Notification
        notificationService.notifyUser(
                user,
                "Complaint Filed: " + savedComplaint.getComplaintNumber(),
                "Incident on " + savedComplaint.getPlatform() + " filed with " + validFiles.size() + " screenshot proof(s). Automated triage assessed preliminary severity: " + analysis.getDetectedSeverity() + ".",
                "SUBMISSION",
                savedComplaint.getComplaintNumber()
        );

        ComplaintResponse response = new ComplaintResponse(
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
        response.setPostUrl(savedComplaint.getPostUrl());
        response.setReporterName(savedComplaint.getReporterName());
        return response;
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
        response.setPostUrl(complaint.getPostUrl());
        response.setThreatLevel(complaint.getThreatLevel());
        response.setPriority(complaint.getPriority());
        response.setStatus(complaint.getStatus());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setIncidentDateTime(complaint.getIncidentDateTime());
        response.setAdminRemarks(complaint.getAdminRemarks());
        response.setReporterName(complaint.getReporterName());

        if (complaint.getUser() != null) {
            response.setUserId(complaint.getUser().getId());
            response.setUserName((complaint.getReporterName() != null && !complaint.getReporterName().isBlank())
                    ? complaint.getReporterName()
                    : complaint.getUser().getName());
            response.setUserEmail(complaint.getUser().getEmail());
        } else if (complaint.getReporterName() != null && !complaint.getReporterName().isBlank()) {
            response.setUserName(complaint.getReporterName());
        }

        response.setEvidence(evidenceItems);
        response.setPreliminaryAnalysis(analysisInfo);

        return response;
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getAllComplaints() {
        List<Complaint> complaints = complaintRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return complaints.stream()
                .map(c -> {
                    List<Evidence> evidenceList = evidenceRepository.findByComplaintId(c.getId());
                    List<ComplaintDetailResponse.EvidenceItem> evidenceItems = evidenceList.stream()
                            .map(e -> new ComplaintDetailResponse.EvidenceItem(
                                    e.getId(),
                                    e.getFileName(),
                                    e.getFilePath(),
                                    e.getVerificationStatus(),
                                    e.getUploadedAt()
                            ))
                            .collect(Collectors.toList());

                    String repName = (c.getReporterName() != null && !c.getReporterName().isBlank())
                            ? c.getReporterName()
                            : ((c.getUser() != null) ? c.getUser().getName() : "Unknown");

                    ComplaintSummaryResponse resp = new ComplaintSummaryResponse(
                            c.getId(),
                            c.getComplaintNumber(),
                            c.getTitle(),
                            repName,
                            c.getThreatLevel(),
                            c.getPriority(),
                            c.getStatus(),
                            c.getCreatedAt()
                    );
                    resp.setPlatform(c.getPlatform());
                    resp.setDescription(c.getDescription());
                    resp.setPostUrl(c.getPostUrl());
                    resp.setReporterName(c.getReporterName());
                    resp.setEvidence(evidenceItems);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ComplaintSummaryResponse> getComplaintsByUserId(Long userId) {
        List<Complaint> complaints = complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return complaints.stream()
                .map(c -> {
                    List<Evidence> evidenceList = evidenceRepository.findByComplaintId(c.getId());
                    List<ComplaintDetailResponse.EvidenceItem> evidenceItems = evidenceList.stream()
                            .map(e -> new ComplaintDetailResponse.EvidenceItem(
                                    e.getId(),
                                    e.getFileName(),
                                    e.getFilePath(),
                                    e.getVerificationStatus(),
                                    e.getUploadedAt()
                            ))
                            .collect(Collectors.toList());

                    String repName = (c.getReporterName() != null && !c.getReporterName().isBlank())
                            ? c.getReporterName()
                            : ((c.getUser() != null) ? c.getUser().getName() : "Unknown");

                    ComplaintSummaryResponse resp = new ComplaintSummaryResponse(
                            c.getId(),
                            c.getComplaintNumber(),
                            c.getTitle(),
                            repName,
                            c.getThreatLevel(),
                            c.getPriority(),
                            c.getStatus(),
                            c.getCreatedAt()
                    );
                    resp.setPlatform(c.getPlatform());
                    resp.setDescription(c.getDescription());
                    resp.setPostUrl(c.getPostUrl());
                    resp.setReporterName(c.getReporterName());
                    resp.setEvidence(evidenceItems);
                    return resp;
                })
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
