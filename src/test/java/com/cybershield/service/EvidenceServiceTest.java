package com.cybershield.service;

import com.cybershield.dto.EvidenceResponse;
import com.cybershield.model.Complaint;
import com.cybershield.model.Evidence;
import com.cybershield.repository.ComplaintRepository;
import com.cybershield.repository.EvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EvidenceService evidenceService;

    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        testComplaint = new Complaint();
        testComplaint.setId(10L);
        testComplaint.setComplaintNumber("CS-10010");
        testComplaint.setTitle("Sample cyberbullying incident");
    }

    @Test
    @DisplayName("File Validation: Accepts PNG, JPG, JPEG, and WebP files")
    void testAcceptsValidImageExtensions() {
        MockMultipartFile png = new MockMultipartFile("file", "test.png", "image/png", new byte[100]);
        MockMultipartFile jpg = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[100]);
        MockMultipartFile jpeg = new MockMultipartFile("file", "test.jpeg", "image/jpeg", new byte[100]);
        MockMultipartFile webp = new MockMultipartFile("file", "test.webp", "image/webp", new byte[100]);

        assertDoesNotThrow(() -> EvidenceService.validateScreenshotFile(png));
        assertDoesNotThrow(() -> EvidenceService.validateScreenshotFile(jpg));
        assertDoesNotThrow(() -> EvidenceService.validateScreenshotFile(jpeg));
        assertDoesNotThrow(() -> EvidenceService.validateScreenshotFile(webp));
    }

    @Test
    @DisplayName("File Validation: Rejects executable, script, or non-image files")
    void testRejectsInvalidFileExtensions() {
        MockMultipartFile exe = new MockMultipartFile("file", "malicious.exe", "application/octet-stream", new byte[100]);
        MockMultipartFile sh = new MockMultipartFile("file", "script.sh", "text/plain", new byte[100]);
        MockMultipartFile pdf = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[100]);
        MockMultipartFile txt = new MockMultipartFile("file", "notes.txt", "text/plain", new byte[100]);

        assertThrows(IllegalArgumentException.class, () -> EvidenceService.validateScreenshotFile(exe));
        assertThrows(IllegalArgumentException.class, () -> EvidenceService.validateScreenshotFile(sh));
        assertThrows(IllegalArgumentException.class, () -> EvidenceService.validateScreenshotFile(pdf));
        assertThrows(IllegalArgumentException.class, () -> EvidenceService.validateScreenshotFile(txt));
    }

    @Test
    @DisplayName("File Validation: Rejects files larger than 10MB")
    void testRejectsOversizedFile() {
        // 11MB file
        byte[] largeBytes = new byte[11 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile("file", "huge.png", "image/png", largeBytes);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                EvidenceService.validateScreenshotFile(largeFile));
        assertTrue(ex.getMessage().contains("10MB"));
    }

    @Test
    @DisplayName("Multiple Evidence Upload: Upload multiple files associates all with complaint")
    void testUploadMultipleEvidence() throws IOException {
        MockMultipartFile file1 = new MockMultipartFile("files", "snap1.png", "image/png", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "snap2.jpg", "image/jpeg", "content2".getBytes());
        List<MultipartFile> files = List.of(file1, file2);

        when(complaintRepository.findById(10L)).thenReturn(Optional.of(testComplaint));
        when(evidenceRepository.save(any(Evidence.class))).thenAnswer(inv -> {
            Evidence ev = inv.getArgument(0);
            ev.setId(100L);
            return ev;
        });

        List<EvidenceResponse> responses = evidenceService.uploadMultipleEvidence(10L, files);

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("snap1.png", responses.get(0).getFileName());
        assertEquals("snap2.jpg", responses.get(1).getFileName());

        verify(evidenceRepository, times(2)).save(any(Evidence.class));
    }

    @Test
    @DisplayName("Multiple Evidence Upload: Rejects total payload exceeding 50MB")
    void testRejectsTotalSizeExceeding50MB() {
        // Mock 6 files each 9MB = 54MB total
        byte[] nineMb = new byte[9 * 1024 * 1024];
        List<MultipartFile> largeBatch = List.of(
                new MockMultipartFile("files", "f1.png", "image/png", nineMb),
                new MockMultipartFile("files", "f2.png", "image/png", nineMb),
                new MockMultipartFile("files", "f3.png", "image/png", nineMb),
                new MockMultipartFile("files", "f4.png", "image/png", nineMb),
                new MockMultipartFile("files", "f5.png", "image/png", nineMb),
                new MockMultipartFile("files", "f6.png", "image/png", nineMb)
        );

        when(complaintRepository.findById(10L)).thenReturn(Optional.of(testComplaint));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                evidenceService.uploadMultipleEvidence(10L, largeBatch));
        assertTrue(ex.getMessage().contains("50MB"));
    }
}
