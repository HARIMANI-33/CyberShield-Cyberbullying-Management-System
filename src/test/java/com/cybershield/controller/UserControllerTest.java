package com.cybershield.controller;

import com.cybershield.dto.RegisterUserRequest;
import com.cybershield.dto.UserResponse;
import com.cybershield.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void testRegisterUserReturnsCreated() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
                "Test User",
                "test@gmail.com",
                "9876543210",
                "test123"
        );

        UserResponse response = new UserResponse(
                1L,
                "Test User",
                "test@gmail.com",
                "9876543210",
                "USER",
                LocalDateTime.now()
        );

        when(userService.registerUser(any(RegisterUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void testLoginUserSuccess() throws Exception {
        com.cybershield.dto.LoginRequest loginReq = new com.cybershield.dto.LoginRequest("test@gmail.com", "password123");
        UserResponse response = new UserResponse(
                1L,
                "Test User",
                "test@gmail.com",
                "9876543210",
                "USER",
                LocalDateTime.now()
        );

        when(userService.loginUser("test@gmail.com", "password123")).thenReturn(response);

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"));
    }
}
