package com.cybershield.service;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.repository.AbusiveWordRepository;
import com.cybershield.repository.ComplaintAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeverityAnalysisService {

    private final AbusiveWordRepository abusiveWordRepository;
    private final ComplaintAnalysisRepository complaintAnalysisRepository;

    public SeverityAnalysisService(AbusiveWordRepository abusiveWordRepository,
                                   ComplaintAnalysisRepository complaintAnalysisRepository) {
        this.abusiveWordRepository = abusiveWordRepository;
        this.complaintAnalysisRepository = complaintAnalysisRepository;
    }

    @Transactional
    public ComplaintAnalysis analyzeComplaint(Complaint complaint) {
        String description = (complaint.getDescription() != null) ? complaint.getDescription().toLowerCase() : "";

        List<AbusiveWord> activeWords = abusiveWordRepository.findByActiveTrue();

        int matchedCount = 0;
        int riskScore = 0;

        for (AbusiveWord word : activeWords) {
            if (word.getKeyword() != null && !word.getKeyword().isBlank()) {
                String keywordLower = word.getKeyword().toLowerCase().trim();
                if (description.contains(keywordLower)) {
                    matchedCount++;
                    int score = (word.getScore() != null) ? word.getScore() : 1;
                    riskScore += score;
                }
            }
        }

        String detectedSeverity = calculateSeverity(riskScore);
        String priority = calculatePriority(detectedSeverity);

        complaint.setThreatLevel(detectedSeverity);
        complaint.setPriority(priority);

        ComplaintAnalysis analysis = new ComplaintAnalysis(
                matchedCount,
                riskScore,
                detectedSeverity,
                priority,
                complaint
        );

        return complaintAnalysisRepository.save(analysis);
    }

    public String calculateSeverity(int riskScore) {
        if (riskScore >= 9) {
            return "CRITICAL";
        } else if (riskScore >= 6) {
            return "HIGH";
        } else if (riskScore >= 3) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    public String calculatePriority(String severity) {
        if ("HIGH".equalsIgnoreCase(severity) || "CRITICAL".equalsIgnoreCase(severity)) {
            return "IMMEDIATE_ATTENTION";
        } else {
            return "NORMAL_REVIEW";
        }
    }
}
