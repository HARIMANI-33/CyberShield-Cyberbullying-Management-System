package com.cybershield.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ComplaintDetailResponse {

    private Long id;
    private String complaintNumber;
    private String title;
    private String category;
    private String platform;
    private String description;
    private String threatLevel;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private String userName;
    private String userEmail;
    private String reporterName;
    private String postUrl;
    private List<EvidenceItem> evidence;
    private PreliminaryAnalysisInfo preliminaryAnalysis;

    public ComplaintDetailResponse() {
    }

    public static class EvidenceItem {
        private Long id;
        private String fileName;
        private String filePath;
        private String verificationStatus;
        private LocalDateTime uploadedAt;

        public EvidenceItem() {
        }

        public EvidenceItem(Long id, String fileName, String filePath, String verificationStatus, LocalDateTime uploadedAt) {
            this.id = id;
            this.fileName = fileName;
            this.filePath = filePath;
            this.verificationStatus = verificationStatus;
            this.uploadedAt = uploadedAt;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getVerificationStatus() {
            return verificationStatus;
        }

        public void setVerificationStatus(String verificationStatus) {
            this.verificationStatus = verificationStatus;
        }

        public LocalDateTime getUploadedAt() {
            return uploadedAt;
        }

        public void setUploadedAt(LocalDateTime uploadedAt) {
            this.uploadedAt = uploadedAt;
        }
    }

    public static class PreliminaryAnalysisInfo {
        private Integer riskScore;
        private String detectedSeverity;
        private String priority;
        private Integer matchedCount;
        private LocalDateTime analysisDate;

        public PreliminaryAnalysisInfo() {
        }

        public PreliminaryAnalysisInfo(Integer riskScore, String detectedSeverity, String priority,
                                       Integer matchedCount, LocalDateTime analysisDate) {
            this.riskScore = riskScore;
            this.detectedSeverity = detectedSeverity;
            this.priority = priority;
            this.matchedCount = matchedCount;
            this.analysisDate = analysisDate;
        }

        public Integer getRiskScore() {
            return riskScore;
        }

        public void setRiskScore(Integer riskScore) {
            this.riskScore = riskScore;
        }

        public String getDetectedSeverity() {
            return detectedSeverity;
        }

        public void setDetectedSeverity(String detectedSeverity) {
            this.detectedSeverity = detectedSeverity;
        }

        public String getPriority() {
            return priority;
        }

        public void setPriority(String priority) {
            this.priority = priority;
        }

        public Integer getMatchedCount() {
            return matchedCount;
        }

        public void setMatchedCount(Integer matchedCount) {
            this.matchedCount = matchedCount;
        }

        public LocalDateTime getAnalysisDate() {
            return analysisDate;
        }

        public void setAnalysisDate(LocalDateTime analysisDate) {
            this.analysisDate = analysisDate;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(String threatLevel) {
        this.threatLevel = threatLevel;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public List<EvidenceItem> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<EvidenceItem> evidence) {
        this.evidence = evidence;
    }

    public PreliminaryAnalysisInfo getPreliminaryAnalysis() {
        return preliminaryAnalysis;
    }

    public void setPreliminaryAnalysis(PreliminaryAnalysisInfo preliminaryAnalysis) {
        this.preliminaryAnalysis = preliminaryAnalysis;
    }

    private Long userId;
    private LocalDateTime incidentDateTime;
    private String adminRemarks;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getIncidentDateTime() {
        return incidentDateTime;
    }

    public void setIncidentDateTime(LocalDateTime incidentDateTime) {
        this.incidentDateTime = incidentDateTime;
    }

    public String getAdminRemarks() {
        return adminRemarks;
    }

    public void setAdminRemarks(String adminRemarks) {
        this.adminRemarks = adminRemarks;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
}
