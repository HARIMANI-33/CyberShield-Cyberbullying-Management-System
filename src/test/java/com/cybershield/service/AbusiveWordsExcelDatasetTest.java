package com.cybershield.service;

import com.cybershield.model.AbusiveWord;
import com.cybershield.model.Complaint;
import com.cybershield.model.ComplaintAnalysis;
import com.cybershield.repository.AbusiveWordRepository;
import com.cybershield.repository.ComplaintAnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbusiveWordsExcelDatasetTest {

    @Mock
    private AbusiveWordRepository abusiveWordRepository;

    @Mock
    private ComplaintAnalysisRepository complaintAnalysisRepository;

    private AbusiveWordsExcelImporter excelImporter;
    private SeverityAnalysisService severityAnalysisService;

    private List<AbusiveWord> inMemoryDb;

    @BeforeEach
    void setUp() {
        inMemoryDb = new ArrayList<>();
        excelImporter = new AbusiveWordsExcelImporter(abusiveWordRepository);
        severityAnalysisService = new SeverityAnalysisService(abusiveWordRepository, complaintAnalysisRepository);
    }

    @Test
    void testExcelImportParsesAllRecordsAndPreventsDuplicates() {
        // Mock repository behaviors simulating DB operations
        Map<String, AbusiveWord> storage = new HashMap<>();

        when(abusiveWordRepository.findByKeywordIgnoreCase(anyString())).thenAnswer(invocation -> {
            String kw = invocation.getArgument(0, String.class);
            return Optional.ofNullable(storage.get(kw.toLowerCase()));
        });

        when(abusiveWordRepository.save(any(AbusiveWord.class))).thenAnswer(invocation -> {
            AbusiveWord w = invocation.getArgument(0, AbusiveWord.class);
            storage.put(w.getKeyword().toLowerCase(), w);
            return w;
        });

        // 1st Import
        AbusiveWordsExcelImporter.ImportResult firstRun = excelImporter.importDatasetFromExcel();
        assertTrue(firstRun.totalProcessed >= 65, "Expected at least 65 records processed from Excel, found: " + firstRun.totalProcessed);
        assertEquals(firstRun.totalProcessed, firstRun.insertedCount);
        assertEquals(0, firstRun.updatedCount);
        int initialSize = storage.size();

        // 2nd Import - must update instead of duplicating
        AbusiveWordsExcelImporter.ImportResult secondRun = excelImporter.importDatasetFromExcel();
        assertEquals(firstRun.totalProcessed, secondRun.totalProcessed);
        assertEquals(0, secondRun.insertedCount, "Second run must not insert duplicate records");
        assertEquals(firstRun.totalProcessed, secondRun.updatedCount);
        assertEquals(initialSize, storage.size(), "Database size must not grow on duplicate import");

        // Verify key records exist with correct mapped fields
        AbusiveWord killYou = storage.get("i will kill you");
        assertNotNull(killYou);
        assertEquals(10, killYou.getScore());
        assertEquals("CRITICAL", killYou.getSeverity());
        assertEquals("Threat", killYou.getCategory());
        assertEquals("Immediate Attention", killYou.getRecommendedAction());

        AbusiveWord poda = storage.get("poda");
        assertNotNull(poda);
        assertEquals("Tamil", poda.getLanguage());
        assertEquals(3, poda.getScore());
        assertEquals("MEDIUM", poda.getSeverity());

        AbusiveWord pieceOfShit = storage.get("piece of shit");
        assertNotNull(pieceOfShit);
        assertEquals(6, pieceOfShit.getScore());
        assertEquals("HIGH", pieceOfShit.getSeverity());
    }

    @Test
    void testTextNormalizationAndPhraseMatching() {
        List<AbusiveWord> activeWords = List.of(
                new AbusiveWord("i will kill you", "English", "Threat", "CRITICAL", 10, "Immediate Attention", true),
                new AbusiveWord("piece of shit", "English", "Profanity / Insult", "HIGH", 6, "Immediate Attention", true),
                new AbusiveWord("fuck you", "English", "Profanity / Insult", "4", 4, "Normal Review", true),
                new AbusiveWord("poda", "Tamil", "Tamil insult", "MEDIUM", 3, "Normal Review", true),
                new AbusiveWord("mokka", "Tamil", "Tamil insult", "LOW", 2, "Normal Review", true)
        );

        when(abusiveWordRepository.findByActiveTrue()).thenReturn(activeWords);
        when(complaintAnalysisRepository.save(any(ComplaintAnalysis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 1. Critical Threat with punctuation & uppercase
        Complaint c1 = new Complaint();
        c1.setDescription("Hey you, I WILL KILL YOU!!! Do you hear me???");
        ComplaintAnalysis a1 = severityAnalysisService.analyzeComplaint(c1);
        assertEquals("CRITICAL", a1.getDetectedSeverity());
        assertEquals(10, a1.getRiskScore());
        assertEquals("Threat", a1.getDetectedCategory());
        assertTrue(a1.getMatchedTerms().contains("i will kill you"));

        // 2. High severity multi-word phrase with extra whitespace
        Complaint c2 = new Complaint();
        c2.setDescription("You are a   PIECE   OF   SHIT   leave me alone");
        ComplaintAnalysis a2 = severityAnalysisService.analyzeComplaint(c2);
        assertEquals("HIGH", a2.getDetectedSeverity());
        assertEquals(6, a2.getRiskScore());
        assertEquals("Profanity / Insult", a2.getDetectedCategory());

        // 3. Medium severity Tamil word
        Complaint c3 = new Complaint();
        c3.setDescription("dei poda enkitta pesadha");
        ComplaintAnalysis a3 = severityAnalysisService.analyzeComplaint(c3);
        assertEquals("MEDIUM", a3.getDetectedSeverity());
        assertEquals(3, a3.getRiskScore());

        // 4. Low severity isolated insult
        Complaint c4 = new Complaint();
        c4.setDescription("romba mokka post idhu");
        ComplaintAnalysis a4 = severityAnalysisService.analyzeComplaint(c4);
        assertEquals("LOW", a4.getDetectedSeverity());
        assertEquals(2, a4.getRiskScore());

        // 5. Clean text without abusive terms
        Complaint c5 = new Complaint();
        c5.setDescription("Could you please review my login credentials? Thank you.");
        ComplaintAnalysis a5 = severityAnalysisService.analyzeComplaint(c5);
        assertEquals("LOW", a5.getDetectedSeverity());
        assertEquals(0, a5.getRiskScore());
        assertEquals(0, a5.getMatchedCount());
    }
}
