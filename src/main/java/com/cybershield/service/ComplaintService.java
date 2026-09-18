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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    private static final Set<String> ALLOWED_STATUSES = Set.of(
            "SUBMITTED",
            "UNDER_REVIEW",
            "EVIDENCE_VERIFICATION",
            "INVESTIGATION",
            "RESOLVED"
    );

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final SeverityAnalysisService severityAnalysisService;
    private final EvidenceRepository evidenceRepository;
    private final ComplaintAnalysisRepository complaintAnalysisRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            UserRepository userRepository,
                            SeverityAnalysisService severityAnalysisService,
                            EvidenceRepository evidenceRepository,
                            ComplaintAnalysisRepository complaintAnalysisRepository) {
        this.complaintRepository = complaintRepository;
        this.userRepository = userRepository;
        this.severityAnalysisService = severityAnalysisService;
        this.evidenceRepository = evidenceRepository;
        this.complaintAnalysisRepository = complaintAnalysisRepository;
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
        complaint.setTitle(request.getTitle());
        complaint.setCategory(request.getCategory());
        complaint.setPlatform(request.getPlatform());
        complaint.setDescription(request.getDescription());
        complaint.setUser(user);
        complaint.setStatus("SUBMITTED");

        // Save initial complaint
        Complaint savedComplaint = complaintRepository.save(complaint);

        // Perform automatic severity analysis
        ComplaintAnalysis analysis = severityAnalysisService.analyzeComplaint(savedComplaint);

        // Save updated complaint threatLevel and priority
        complaintRepository.save(savedComplaint);

        return new ComplaintResponse(
                savedComplaint.getId(),
                savedComplaint.getComplaintNumber(),
                "Complaint registered successfully",
                analysis.getDetectedSeverity(),
                analysis.getRiskScore(),
                analysis.getPriority(),
                savedComplaint.getStatus(),
                "Preliminary automated assessment, not a final decision. Subject to officer investigation."
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

        if (complaint.getUser() != null) {
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

    @Transactional
    public ComplaintDetailResponse updateStatus(Long id, String newStatus) {
        if (newStatus == null || !ALLOWED_STATUSES.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status: " + newStatus + ". Allowed: " + ALLOWED_STATUSES);
        }

        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + id));

        complaint.setStatus(newStatus.toUpperCase());
        complaintRepository.save(complaint);

        return getComplaintDetails(id);
    }

    private synchronized String generateComplaintNumber() {
        long count = complaintRepository.count();
        long nextSeq = 10001 + count;
        String candidate = "CS-" + nextSeq;
        while (complaintRepository.existsByComplaintNumber(candidate)) {
            nextSeq++;
            candidate = "CS-" + nextSeq;
        }
        return candidate;
    }
}
