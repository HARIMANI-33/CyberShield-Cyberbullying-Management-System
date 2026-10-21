package com.cybershield.service;

import com.cybershield.dto.CreateComplaintRequest;
import com.cybershield.dto.ComplaintResponse;
import com.cybershield.model.Complaint;
import com.cybershield.model.Evidence;
import com.cybershield.model.User;
import com.cybershield.repository.ComplaintRepository;
import com.cybershield.repository.EvidenceRepository;
import com.cybershield.repository.NotificationRepository;
import com.cybershield.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.cybershield.repository.ComplaintAnalysisRepository complaintAnalysisRepository;

    @Mock
    private SeverityAnalysisService severityAnalysisService;

    @Mock
    private EvidenceService evidenceService;

    @InjectMocks
    private ComplaintService complaintService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test Student");
        testUser.setEmail("student@university.edu");
    }

    @Test
    @DisplayName("URL Validation: Valid HTTP and HTTPS URLs are accepted")
    void testValidHttpAndHttpsUrls() {
        assertTrue(ComplaintService.isValidHttpOrHttpsUrl("https://www.instagram.com/p/C-xyz123/"));
        assertTrue(ComplaintService.isValidHttpOrHttpsUrl("http://facebook.com/photo.php?fbid=12345"));
        assertTrue(ComplaintService.isValidHttpOrHttpsUrl("https://twitter.com/user/status/987654321"));
        assertTrue(ComplaintService.isValidHttpOrHttpsUrl("https://sub.domain.org/path/item?id=10&ref=cyber"));
    }

    @Test
    @DisplayName("URL Validation: Invalid and unsafe schemes are rejected")
    void testInvalidAndUnsafeUrlSchemesRejected() {
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl(null));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl(""));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("   "));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("javascript:alert(1)"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("data:text/html,<script>alert(1)</script>"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("ftp://ftp.example.com/file.txt"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("file:///C:/passwords.txt"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("just plain text without protocol"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("https://"));
        assertFalse(ComplaintService.isValidHttpOrHttpsUrl("http://"));
    }

    @Test
    @DisplayName("Mandatory Fields: Throws IllegalArgumentException on missing or empty required fields")
    void testMandatoryFieldsValidation() {
        // Missing title
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.validateComplaintFields("", "Harassment", "Instagram", "Description", "https://instagram.com/p/123"));
        assertTrue(ex1.getMessage().toLowerCase().contains("title"));

        // Missing category
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.validateComplaintFields("Title", "   ", "Instagram", "Description", "https://instagram.com/p/123"));
        assertTrue(ex2.getMessage().toLowerCase().contains("category"));

        // Missing platform
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.validateComplaintFields("Title", "Harassment", "", "Description", "https://instagram.com/p/123"));
        assertTrue(ex3.getMessage().toLowerCase().contains("platform"));

        // Missing or whitespace-only description
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.validateComplaintFields("Title", "Harassment", "Instagram", "   \t\n  ", "https://instagram.com/p/123"));
        assertTrue(ex4.getMessage().toLowerCase().contains("description"));

        // Missing or invalid Post URL
        IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class, () ->
                complaintService.validateComplaintFields("Title", "Harassment", "Instagram", "Valid narrative", "javascript:void(0)"));
        assertTrue(ex5.getMessage().toLowerCase().contains("post url"));
    }

    @Test
    @DisplayName("Feature 1 & 2: User can submit one complaint with multiple screenshots and Post URL")
    void testRegisterComplaintWithMultipleScreenshotsAndPostUrl() throws Exception {
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setUserId(1L);
        request.setTitle("Targeted harassment thread");
        request.setCategory("Targeted Harassment");
        request.setPlatform("Instagram");
        request.setDescription("He posted humiliating threats and derogatory slurs under my post.");
        request.setPostUrl("https://www.instagram.com/p/DA123456789/");

        MockMultipartFile file1 = new MockMultipartFile("files", "proof1.png", "image/png", "img-bytes-1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "proof2.jpg", "image/jpeg", "img-bytes-2".getBytes());
        List<MultipartFile> files = List.of(file1, file2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint c = invocation.getArgument(0);
            c.setId(101L);
            c.setComplaintNumber("CS-10101");
            return c;
        });

        Evidence ev1 = new Evidence();
        ev1.setId(1L);
        ev1.setFileName("proof1.png");

        Evidence ev2 = new Evidence();
        ev2.setId(2L);
        ev2.setFileName("proof2.jpg");

        when(evidenceService.saveEvidenceForComplaint(any(Complaint.class), eq(file1))).thenReturn(ev1);
        when(evidenceService.saveEvidenceForComplaint(any(Complaint.class), eq(file2))).thenReturn(ev2);

        com.cybershield.model.ComplaintAnalysis mockAnalysis = new com.cybershield.model.ComplaintAnalysis();
        mockAnalysis.setDetectedSeverity("HIGH");
        mockAnalysis.setRiskScore(8);
        mockAnalysis.setPriority("IMMEDIATE_ATTENTION");
        when(severityAnalysisService.analyzeComplaint(any())).thenReturn(mockAnalysis);

        ComplaintResponse response = complaintService.registerComplaintWithEvidence(request, files);

        assertNotNull(response);
        assertEquals("CS-10101", response.getComplaintNumber());
        assertEquals("https://www.instagram.com/p/DA123456789/", response.getPostUrl());

        // Verify complaint was saved with Post URL
        verify(complaintRepository, atLeastOnce()).save(argThat(c ->
                c.getPostUrl() != null &&
                c.getPostUrl().equals("https://www.instagram.com/p/DA123456789/") &&
                c.getTitle().equals("Targeted harassment thread")
        ));

        // Verify both screenshots were saved
        verify(evidenceService, times(1)).saveEvidenceForComplaint(any(Complaint.class), eq(file1));
        verify(evidenceService, times(1)).saveEvidenceForComplaint(any(Complaint.class), eq(file2));
    }

    @Test
    @DisplayName("Feature 3: Submission without screenshots is rejected and no complaint is created")
    void testRegisterComplaintWithoutScreenshotsIsRejected() {
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setUserId(1L);
        request.setTitle("Threatening comments");
        request.setCategory("Threats");
        request.setPlatform("X");
        request.setDescription("Threatening messages received online");
        request.setPostUrl("https://twitter.com/baduser/status/123456");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                complaintService.registerComplaintWithEvidence(request, Collections.emptyList()));

        assertTrue(ex.getMessage().contains("screenshot"));
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    @DisplayName("Feature 3: Submission with invalid Post URL is rejected without saving complaint")
    void testRegisterComplaintWithUnsafePostUrlIsRejected() {
        CreateComplaintRequest request = new CreateComplaintRequest();
        request.setUserId(1L);
        request.setTitle("Threatening comments");
        request.setCategory("Threats");
        request.setPlatform("X");
        request.setDescription("Threatening messages received online");
        request.setPostUrl("javascript:evilCode()");

        MockMultipartFile file = new MockMultipartFile("files", "proof.png", "image/png", "img-bytes".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                complaintService.registerComplaintWithEvidence(request, List.of(file)));

        assertTrue(ex.getMessage().contains("Post URL"));
        verify(complaintRepository, never()).save(any(Complaint.class));
    }
}
