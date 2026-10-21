package com.cybershield.dto;

public class ComplaintResponse {

    private Long id;
    private String complaintNumber;
    private String message;
    private String detectedSeverity;
    private Integer riskScore;
    private String priority;
    private String status;
    private String assessmentNote;
    private String detectedCategory;
    private String matchedTerms;
    private String postUrl;
    private String reporterName;

    public ComplaintResponse() {
    }

    public ComplaintResponse(Long id, String complaintNumber, String message, String detectedSeverity,
                             Integer riskScore, String priority, String status, String assessmentNote) {
        this(id, complaintNumber, message, detectedSeverity, riskScore, priority, status, assessmentNote, null, null);
    }

    public ComplaintResponse(Long id, String complaintNumber, String message, String detectedSeverity,
                             Integer riskScore, String priority, String status, String assessmentNote,
                             String detectedCategory, String matchedTerms) {
        this.id = id;
        this.complaintNumber = complaintNumber;
        this.message = message;
        this.detectedSeverity = detectedSeverity;
        this.riskScore = riskScore;
        this.priority = priority;
        this.status = status;
        this.assessmentNote = assessmentNote;
        this.detectedCategory = detectedCategory;
        this.matchedTerms = matchedTerms;
    }

    public ComplaintResponse(Long id, String complaintNumber, String message, String detectedSeverity,
                             Integer riskScore, String priority, String assessmentNote) {
        this(id, complaintNumber, message, detectedSeverity, riskScore, priority, "SUBMITTED", assessmentNote);
    }

    public ComplaintResponse(String complaintNumber, String message, String detectedSeverity,
                             Integer riskScore, String priority, String assessmentNote) {
        this(null, complaintNumber, message, detectedSeverity, riskScore, priority, "SUBMITTED", assessmentNote);
    }

    public ComplaintResponse(String complaintNumber, String message, String detectedSeverity,
                             Integer riskScore, String priority, String assessmentNote, String postUrl) {
        this(null, complaintNumber, message, detectedSeverity, riskScore, priority, "SUBMITTED", assessmentNote);
        this.postUrl = postUrl;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetectedSeverity() {
        return detectedSeverity;
    }

    public void setDetectedSeverity(String detectedSeverity) {
        this.detectedSeverity = detectedSeverity;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
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

    public String getAssessmentNote() {
        return assessmentNote;
    }

    public void setAssessmentNote(String assessmentNote) {
        this.assessmentNote = assessmentNote;
    }

    public String getDetectedCategory() {
        return detectedCategory;
    }

    public void setDetectedCategory(String detectedCategory) {
        this.detectedCategory = detectedCategory;
    }

    public String getMatchedTerms() {
        return matchedTerms;
    }

    public void setMatchedTerms(String matchedTerms) {
        this.matchedTerms = matchedTerms;
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
