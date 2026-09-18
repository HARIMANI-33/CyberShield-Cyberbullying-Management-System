package com.cybershield.service;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.repository.AbusiveWordRepository;
import com.cybershield.repository.ComplaintAnalysisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeverityAnalysisServiceTest {

    @Mock
    private AbusiveWordRepository abusiveWordRepository;

    @Mock
    private ComplaintAnalysisRepository complaintAnalysisRepository;

    @InjectMocks
    private SeverityAnalysisService severityAnalysisService;

    private void mockActiveWords() {
        when(abusiveWordRepository.findByActiveTrue()).thenReturn(List.of(
                new AbusiveWord("threat", "English", "Threat", "HIGH", 7, true),
                new AbusiveWord("die", "English", "Harassment", "HIGH", 6, true),
                new AbusiveWord("idiot", "English", "Insult", "LOW", 2, true)
        ));
    }

    @Test
    void testCriticalSeverityDetected() {
        mockActiveWords();
        Complaint complaint = new Complaint();
        complaint.setDescription("He sent a threat to me and told me to die");

        when(complaintAnalysisRepository.save(any(ComplaintAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintAnalysis analysis = severityAnalysisService.analyzeComplaint(complaint);

        assertEquals(2, analysis.getMatchedCount());
        assertEquals(13, analysis.getRiskScore());
        assertEquals("CRITICAL", analysis.getDetectedSeverity());
        assertEquals("IMMEDIATE_ATTENTION", analysis.getPriority());
        assertEquals("CRITICAL", complaint.getThreatLevel());
        assertEquals("IMMEDIATE_ATTENTION", complaint.getPriority());
    }

    @Test
    void testLowSeverityDetected() {
        mockActiveWords();
        Complaint complaint = new Complaint();
        complaint.setDescription("You are an idiot in the comments");

        when(complaintAnalysisRepository.save(any(ComplaintAnalysis.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintAnalysis analysis = severityAnalysisService.analyzeComplaint(complaint);

        assertEquals(1, analysis.getMatchedCount());
        assertEquals(2, analysis.getRiskScore());
        assertEquals("LOW", analysis.getDetectedSeverity());
        assertEquals("NORMAL_REVIEW", analysis.getPriority());
        assertEquals("LOW", complaint.getThreatLevel());
        assertEquals("NORMAL_REVIEW", complaint.getPriority());
    }

    @Test
    void testScoreThresholds() {
        assertEquals("LOW", severityAnalysisService.calculateSeverity(0));
        assertEquals("LOW", severityAnalysisService.calculateSeverity(2));
        assertEquals("MEDIUM", severityAnalysisService.calculateSeverity(3));
        assertEquals("MEDIUM", severityAnalysisService.calculateSeverity(5));
        assertEquals("HIGH", severityAnalysisService.calculateSeverity(6));
        assertEquals("HIGH", severityAnalysisService.calculateSeverity(8));
        assertEquals("CRITICAL", severityAnalysisService.calculateSeverity(9));
        assertEquals("CRITICAL", severityAnalysisService.calculateSeverity(25));
    }
}
