package com.cybershield.dto;

import java.time.LocalDateTime;
import java.util.List;

public class EvidenceResponse {

    private Long id;
    private String message;
    private String fileName;
    private String filePath;
    private String verificationStatus;
    private LocalDateTime uploadedAt;
    private List<EvidenceResponse> items;
    private Integer count;

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

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<EvidenceResponse> getItems() {
        return items;
    }

    public void setItems(List<EvidenceResponse> items) {
        this.items = items;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
