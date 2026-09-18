package com.cybershield.dto;

import java.time.LocalDateTime;

public class EvidenceResponse {

    private Long id;
    private String message;
    private String fileName;
    private String verificationStatus;
    private LocalDateTime uploadedAt;

    public EvidenceResponse() {
    }

    public EvidenceResponse(String message, String fileName, String verificationStatus) {
        this.message = message;
        this.fileName = fileName;
        this.verificationStatus = verificationStatus;
    }

    public EvidenceResponse(Long id, String message, String fileName, String verificationStatus, LocalDateTime uploadedAt) {
        this.id = id;
        this.message = message;
        this.fileName = fileName;
        this.verificationStatus = verificationStatus;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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
