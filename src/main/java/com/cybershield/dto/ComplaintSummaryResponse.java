package com.cybershield.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ComplaintSummaryResponse {

    private Long id;
    private String complaintNumber;
    private String title;
    private String userName;
    private String threatLevel;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private String platform;
    private String description;
    private String postUrl;
    private String reporterName;
    private List<ComplaintDetailResponse.EvidenceItem> evidence;

    public ComplaintSummaryResponse() {
    }

    public ComplaintSummaryResponse(Long id, String complaintNumber, String title, String userName,
                                    String threatLevel, String priority, String status, LocalDateTime createdAt) {
        this.id = id;
        this.complaintNumber = complaintNumber;
        this.title = title;
        this.userName = userName;
        this.threatLevel = threatLevel;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public List<ComplaintDetailResponse.EvidenceItem> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<ComplaintDetailResponse.EvidenceItem> evidence) {
        this.evidence = evidence;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
}
