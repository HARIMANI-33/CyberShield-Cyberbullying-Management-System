package com.cybershield.dto;

public class CreateComplaintRequest {

    private Long userId;
    private String title;
    private String category;
    private String platform;
    private String description;
    private String postUrl;
    private String reporterName;

    public CreateComplaintRequest() {
    }

    public CreateComplaintRequest(Long userId, String title, String category, String platform, String description) {
        this(userId, title, category, platform, description, null);
    }

    public CreateComplaintRequest(Long userId, String title, String category, String platform, String description, String postUrl) {
        this(userId, title, category, platform, description, postUrl, null);
    }

    public CreateComplaintRequest(Long userId, String title, String category, String platform, String description, String postUrl, String reporterName) {
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.platform = platform;
        this.description = description;
        this.postUrl = postUrl;
        this.reporterName = reporterName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    private java.time.LocalDateTime incidentDateTime;

    public java.time.LocalDateTime getIncidentDateTime() {
        return incidentDateTime;
    }

    public void setIncidentDateTime(java.time.LocalDateTime incidentDateTime) {
        this.incidentDateTime = incidentDateTime;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
}
