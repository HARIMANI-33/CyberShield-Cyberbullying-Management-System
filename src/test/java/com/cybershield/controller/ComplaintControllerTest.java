package com.cybershield.controller;

import com.cybershield.dto.*;
import com.cybershield.service.ComplaintService;
import com.cybershield.service.EvidenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplaintController.class)
class ComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private EvidenceService evidenceService;

    @Test
    void testCreateComplaintReturnsCreatedWithAnalysis() throws Exception {
        CreateComplaintRequest request = new CreateComplaintRequest(
                1L,
                "Abusive messages received",
                "Abusive Messages",
                "Instagram",
                "He sent a threat to me"
        );

        ComplaintResponse response = new ComplaintResponse(
                "CS-10001",
                "Complaint registered successfully",
                "HIGH",
                7,
                "IMMEDIATE_ATTENTION",
                "Preliminary automated assessment, not a final decision. Subject to officer investigation."
        );

        when(complaintService.registerComplaint(any(CreateComplaintRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/complaints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintNumber").value("CS-10001"))
                .andExpect(jsonPath("$.message").value("Complaint registered successfully"))
                .andExpect(jsonPath("$.detectedSeverity").value("HIGH"))
                .andExpect(jsonPath("$.riskScore").value(7))
                .andExpect(jsonPath("$.priority").value("IMMEDIATE_ATTENTION"));
    }

    @Test
    void testUploadEvidence() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "screenshot.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake image content".getBytes()
        );

        EvidenceResponse response = new EvidenceResponse(
                1L,
                "Evidence uploaded successfully",
                "screenshot.png",
                "PENDING",
                LocalDateTime.now()
        );

        when(evidenceService.uploadEvidence(eq(1L), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/complaints/1/evidence")
                        .file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Evidence uploaded successfully"))
                .andExpect(jsonPath("$.fileName").value("screenshot.png"))
                .andExpect(jsonPath("$.verificationStatus").value("PENDING"));
    }

    @Test
    void testCreateComplaintMultipartWithMultipleScreenshotsAndPostUrl() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "shot1.png",
                MediaType.IMAGE_PNG_VALUE,
                "img-1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "shot2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "img-2".getBytes()
        );

        ComplaintResponse response = new ComplaintResponse(
                "CS-20002",
                "Complaint and 2 evidence screenshot(s) registered successfully",
                "HIGH",
                8,
                "IMMEDIATE_ATTENTION",
                "Preliminary automated assessment",
                "https://www.instagram.com/p/C-abc12345/"
        );

        when(complaintService.registerComplaintWithEvidence(any(CreateComplaintRequest.class), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/complaints")
                        .file(file1)
                        .file(file2)
                        .param("userId", "1")
                        .param("title", "Targeted harassment campaign")
                        .param("category", "Targeted Harassment")
                        .param("platform", "Instagram")
                        .param("description", "Violent threats posted repeatedly on my profile.")
                        .param("postUrl", "https://www.instagram.com/p/C-abc12345/"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.complaintNumber").value("CS-20002"))
                .andExpect(jsonPath("$.postUrl").value("https://www.instagram.com/p/C-abc12345/"))
                .andExpect(jsonPath("$.detectedSeverity").value("HIGH"));
    }

    @Test
    void testCreateComplaintMultipartRejectsMissingFields() throws Exception {
        when(complaintService.registerComplaintWithEvidence(any(), any()))
                .thenThrow(new IllegalArgumentException("Post URL is mandatory and must be a valid HTTP or HTTPS address"));

        mockMvc.perform(multipart("/api/complaints")
                        .param("userId", "1")
                        .param("title", "Title")
                        .param("category", "Harassment")
                        .param("platform", "Instagram")
                        .param("description", "Some description")
                        .param("postUrl", "invalid-url"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Post URL is mandatory and must be a valid HTTP or HTTPS address"));
    }

    @Test
    void testUploadMultipleEvidenceEndpoint() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "img1.png", MediaType.IMAGE_PNG_VALUE, "a".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "img2.png", MediaType.IMAGE_PNG_VALUE, "b".getBytes());

        EvidenceResponse item1 = new EvidenceResponse(
                1L,
                "Evidence uploaded successfully",
                "img1.png",
                "PENDING",
                LocalDateTime.now()
        );
        EvidenceResponse item2 = new EvidenceResponse(
                2L,
                "Evidence uploaded successfully",
                "img2.png",
                "PENDING",
                LocalDateTime.now()
        );

        when(evidenceService.uploadMultipleEvidence(eq(1L), any())).thenReturn(List.of(item1, item2));

        mockMvc.perform(multipart("/api/complaints/1/evidence")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.message").value("Evidence uploaded successfully"));
    }

    @Test
    void testGetComplaintById() throws Exception {
        ComplaintDetailResponse response = new ComplaintDetailResponse();
        response.setId(1L);
        response.setComplaintNumber("CS-10001");
        response.setTitle("Abusive messages");
        response.setCategory("Abusive Messages");
        response.setPlatform("Instagram");
        response.setDescription("Sample text");
        response.setThreatLevel("HIGH");
        response.setPriority("IMMEDIATE_ATTENTION");
        response.setStatus("SUBMITTED");
        response.setCreatedAt(LocalDateTime.now());
        response.setUserName("Test User");
        response.setPreliminaryAnalysis(new ComplaintDetailResponse.PreliminaryAnalysisInfo(
                7, "HIGH", "IMMEDIATE_ATTENTION", 1, LocalDateTime.now()
        ));

        when(complaintService.getComplaintDetails(1L)).thenReturn(response);

        mockMvc.perform(get("/api/complaints/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.complaintNumber").value("CS-10001"))
                .andExpect(jsonPath("$.threatLevel").value("HIGH"))
                .andExpect(jsonPath("$.preliminaryAnalysis.riskScore").value(7));
    }

    @Test
    void testGetAllComplaints() throws Exception {
        ComplaintSummaryResponse summary = new ComplaintSummaryResponse(
                1L,
                "CS-10001",
                "Abusive messages",
                "Test User",
                "HIGH",
                "IMMEDIATE_ATTENTION",
                "SUBMITTED",
                LocalDateTime.now()
        );

        when(complaintService.getAllComplaints()).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/complaints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].complaintNumber").value("CS-10001"))
                .andExpect(jsonPath("$[0].userName").value("Test User"));
    }

    @Test
    void testUpdateStatus() throws Exception {
        UpdateStatusRequest request = new UpdateStatusRequest("UNDER_REVIEW");

        ComplaintDetailResponse response = new ComplaintDetailResponse();
        response.setId(1L);
        response.setComplaintNumber("CS-10001");
        response.setStatus("UNDER_REVIEW");

        when(complaintService.updateStatus(eq(1L), eq("UNDER_REVIEW"))).thenReturn(response);

        mockMvc.perform(put("/api/complaints/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));
    }
}
