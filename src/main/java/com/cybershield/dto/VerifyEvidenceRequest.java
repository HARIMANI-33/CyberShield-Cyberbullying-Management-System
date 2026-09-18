package com.cybershield.dto;

public class VerifyEvidenceRequest {

    private String verificationStatus;

    public VerifyEvidenceRequest() {
    }

    public VerifyEvidenceRequest(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
}
