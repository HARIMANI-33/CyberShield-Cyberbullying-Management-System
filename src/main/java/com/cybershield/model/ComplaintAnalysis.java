package com.cybershield.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaint_analyses")
public class ComplaintAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer matchedCount;

    private Integer riskScore;

    private String detectedSeverity;

    private String priority;

    private String detectedCategory;

    @Column(length = 1000)
    private String matchedTerms;

    @Column(length = 1000)
    private String assessmentNote;

    @Column(nullable = false)
    private LocalDateTime analysisDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", unique = true)
    private Complaint complaint;

    public ComplaintAnalysis() {
    }

    public ComplaintAnalysis(Integer matchedCount, Integer riskScore, String detectedSeverity,
                             String priority, Complaint complaint) {
        this(matchedCount, riskScore, detectedSeverity, priority, null, null, null, complaint);
    }

    public ComplaintAnalysis(Integer matchedCount, Integer riskScore, String detectedSeverity,
                             String priority, String detectedCategory, String matchedTerms,
                             String assessmentNote, Complaint complaint) {
        this.matchedCount = matchedCount;
        this.riskScore = riskScore;
        this.detectedSeverity = detectedSeverity;
        this.priority = priority;
        this.detectedCategory = detectedCategory;
        this.matchedTerms = matchedTerms;
        this.assessmentNote = assessmentNote;
        this.complaint = complaint;
    }

    @PrePersist
    protected void onCreate() {
        if (this.analysisDate == null) {
            this.analysisDate = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(Integer matchedCount) {
        this.matchedCount = matchedCount;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getDetectedSeverity() {
        return detectedSeverity;
    }

    public void setDetectedSeverity(String detectedSeverity) {
        this.detectedSeverity = detectedSeverity;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public String getAssessmentNote() {
        return assessmentNote;
    }

    public void setAssessmentNote(String assessmentNote) {
        this.assessmentNote = assessmentNote;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }

    public Complaint getComplaint() {
        return complaint;
    }

    public void setComplaint(Complaint complaint) {
        this.complaint = complaint;
    }
}
