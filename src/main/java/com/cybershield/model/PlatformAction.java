package com.cybershield.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_actions")
public class PlatformAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String complaintNumber;

    @Column(nullable = false)
    private String platform;

    @Column(nullable = false)
    private String recipient;

    private String reportedAccount;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(nullable = false)
    private String actionStatus = "DRAFT"; // DRAFT, READY, SENT, FAILED

    private String adminOfficerName;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    public PlatformAction() {
    }

    public PlatformAction(Complaint complaint, String complaintNumber, String platform,
                          String recipient, String reportedAccount, String subject,
                          String message, String adminOfficerName) {
        this.complaint = complaint;
        this.complaintNumber = complaintNumber;
        this.platform = platform;
        this.recipient = recipient;
        this.reportedAccount = reportedAccount;
        this.subject = subject;
        this.message = message;
        this.adminOfficerName = adminOfficerName;
        this.actionStatus = "DRAFT";
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.actionStatus == null || this.actionStatus.isBlank()) {
            this.actionStatus = "DRAFT";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
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
