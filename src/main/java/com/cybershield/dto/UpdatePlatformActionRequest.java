package com.cybershield.dto;

public class UpdatePlatformActionRequest {

    private String recipient;
    private String reportedAccount;
    private String subject;
    private String message;
    private String adminNotes;

    public UpdatePlatformActionRequest() {
    }

    public UpdatePlatformActionRequest(String recipient, String reportedAccount, String subject,
                                       String message, String adminNotes) {
        this.recipient = recipient;
        this.reportedAccount = reportedAccount;
        this.subject = subject;
        this.message = message;
        this.adminNotes = adminNotes;
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

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
}
