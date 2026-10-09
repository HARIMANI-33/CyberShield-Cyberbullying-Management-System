package com.cybershield.dto;

public class GeneratePlatformActionRequest {

    private Long complaintId;
    private String platform;
    private String recipient;
    private String reportedAccount;
    private String adminOfficerName;

    public GeneratePlatformActionRequest() {
    }

    public GeneratePlatformActionRequest(Long complaintId, String platform, String recipient,
                                         String reportedAccount, String adminOfficerName) {
        this.complaintId = complaintId;
        this.platform = platform;
        this.recipient = recipient;
        this.reportedAccount = reportedAccount;
        this.adminOfficerName = adminOfficerName;
    }

    public Long getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(Long complaintId) {
        this.complaintId = complaintId;
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

    public String getAdminOfficerName() {
        return adminOfficerName;
    }

    public void setAdminOfficerName(String adminOfficerName) {
        this.adminOfficerName = adminOfficerName;
    }
}
