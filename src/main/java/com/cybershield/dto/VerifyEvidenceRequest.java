package com.cybershield.dto;

public class VerifyEvidenceRequest {

    private String verificationStatus;
    private String remarks;

    public VerifyEvidenceRequest() {
    }

    public VerifyEvidenceRequest(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public VerifyEvidenceRequest(String verificationStatus, String remarks) {
        this.verificationStatus = verificationStatus;
        this.remarks = remarks;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
