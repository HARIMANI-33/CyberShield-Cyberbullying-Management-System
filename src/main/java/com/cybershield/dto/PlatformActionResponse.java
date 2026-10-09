package com.cybershield.dto;

import java.time.LocalDateTime;

public class PlatformActionResponse {

    private Long id;
    private Long complaintId;
    private String complaintNumber;
    private String platform;
    private String recipient;
    private String reportedAccount;
    private String subject;
    private String message;
    private String actionStatus;
    private String adminOfficerName;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public PlatformActionResponse() {
    }

    public PlatformActionResponse(Long id, Long complaintId, String complaintNumber, String platform,
                                  String recipient, String reportedAccount, String subject,
                                  String message, String actionStatus, String adminOfficerName,
                                  String adminNotes, LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.complaintId = complaintId;
        this.complaintNumber = complaintNumber;
        this.platform = platform;
        this.recipient = recipient;
        this.reportedAccount = reportedAccount;
        this.subject = subject;
        this.message = message;
        this.actionStatus = actionStatus;
        this.adminOfficerName = adminOfficerName;
        this.adminNotes = adminNotes;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintNumber() {
        return complaintNumber;
    }

    public void setComplaintNumber(String complaintNumber) {
        this.complaintNumber = complaintNumber;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getReportedAccount() {
        return reportedAccount;
    }

    public void setReportedAccount(String reportedAccount) {
        this.reportedAccount = reportedAccount;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    public String getAdminOfficerName() {
        return adminOfficerName;
    }

    public void setAdminOfficerName(String adminOfficerName) {
        this.adminOfficerName = adminOfficerName;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
