package com.cybershield.service;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.repository.AbusiveWordRepository;
import com.cybershield.repository.ComplaintAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
        String rawText = "";
        if (complaint.getDescription() != null && !complaint.getDescription().isBlank()) {
            rawText = complaint.getDescription();
        } else if (complaint.getTitle() != null && !complaint.getTitle().isBlank()) {
            rawText = complaint.getTitle();
        }

        String normText = normalizeText(rawText);
        String paddedText = " " + normText + " ";
        boolean[] matchedPositions = new boolean[paddedText.length()];

        // Sort abusive keywords by length descending so longer phrases match before sub-words
        List<AbusiveWord> activeWords = new ArrayList<>(abusiveWordRepository.findByActiveTrue());
        activeWords.sort((a, b) -> {
            int lenA = (a.getKeyword() != null) ? a.getKeyword().trim().length() : 0;
            int lenB = (b.getKeyword() != null) ? b.getKeyword().trim().length() : 0;
            return Integer.compare(lenB, lenA);
        });

        int matchedCount = 0;
        int riskScore = 0;
        List<String> matchedTerms = new ArrayList<>();
        Set<String> matchedCategories = new LinkedHashSet<>();
        String highestScoringCategory = null;
        int maxScoreSeen = -1;

        for (AbusiveWord word : activeWords) {
            if (word.getKeyword() != null && !word.getKeyword().isBlank()) {
                String normKeyword = normalizeText(word.getKeyword());
                if (normKeyword.isEmpty()) {
                    continue;
                }

                String paddedKeyword = " " + normKeyword + " ";
                int searchIdx = 0;
                boolean wordMatchedInText = false;

                while ((searchIdx = paddedText.indexOf(paddedKeyword, searchIdx)) != -1) {
                    int endIdx = searchIdx + paddedKeyword.length();
                    boolean alreadyCovered = false;
                    for (int k = searchIdx; k < endIdx; k++) {
                        if (matchedPositions[k]) {
                            alreadyCovered = true;
                            break;
                        }
                    }

                    if (!alreadyCovered) {
                        for (int k = searchIdx; k < endIdx; k++) {
                            matchedPositions[k] = true;
                        }
                        wordMatchedInText = true;
                    }
                    searchIdx += 1;
                }

                if (wordMatchedInText) {
                    matchedCount++;
                    int score = (word.getScore() != null) ? word.getScore() : 1;
                    riskScore += score;
                    matchedTerms.add(word.getKeyword().trim());

                    String cat = (word.getCategory() != null && !word.getCategory().isBlank())
                            ? word.getCategory().trim()
                            : "Harassment";
                    matchedCategories.add(cat);

                    if (score > maxScoreSeen) {
                        maxScoreSeen = score;
                        highestScoringCategory = cat;
                    }
                }
            }
        }

        String detectedSeverity = calculateSeverity(riskScore);
        String priority = calculatePriority(detectedSeverity);

        String primaryCategory = highestScoringCategory;
        if (primaryCategory == null || primaryCategory.isBlank()) {
            primaryCategory = (complaint.getCategory() != null && !complaint.getCategory().isBlank())
                    ? complaint.getCategory()
                    : "General";
        }

        complaint.setThreatLevel(detectedSeverity);
        complaint.setPriority(priority);

        int displayScore = Math.min(riskScore, 10);
        String assessmentNote = String.format(
                "Preliminary Automated Severity Assessment: %s (Risk Score: %d/10). Category: %s. This is only an automated preliminary assessment. The administrator makes the final decision.",
                detectedSeverity, displayScore, primaryCategory
        );

        ComplaintAnalysis analysis = new ComplaintAnalysis(
                matchedCount,
                riskScore,
                detectedSeverity,
                priority,
                primaryCategory,
                String.join(", ", matchedTerms),
                assessmentNote,
                complaint
        );

        return complaintAnalysisRepository.save(analysis);
    }

    public String normalizeText(String input) {
        if (input == null) return "";
        // 1. Lowercase for case-insensitivity
        String normalized = input.toLowerCase();
        // 2. Remove unnecessary punctuation & symbols while preserving unicode letters and digits
        normalized = normalized.replaceAll("[^\\p{L}\\p{Nd}\\s]", " ");
        // 3. Collapse extra whitespace to a single space
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
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
