package com.cybershield.controller;

import com.cybershield.dto.EvidenceResponse;
import com.cybershield.dto.VerifyEvidenceRequest;
import com.cybershield.service.EvidenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvidenceController.class)
class EvidenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EvidenceService evidenceService;

    @Test
    void testVerifyEvidenceReturnsUpdatedStatus() throws Exception {
        VerifyEvidenceRequest request = new VerifyEvidenceRequest("VERIFIED");

        EvidenceResponse response = new EvidenceResponse(
                1L,
                "Evidence verification status updated to VERIFIED",
                "screenshot.png",
                "VERIFIED",
                LocalDateTime.now()
        );

        when(evidenceService.verifyEvidence(eq(1L), eq("VERIFIED"))).thenReturn(response);

        mockMvc.perform(put("/api/evidence/1/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificationStatus").value("VERIFIED"));
    }
}
