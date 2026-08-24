package com.litmus.authPortal.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmus.authPortal.config.authentication.JwtFilter;
import com.litmus.authPortal.config.authentication.SecurityConfig;
import com.litmus.authPortal.dto.auth.AuthRequest;
import com.litmus.authPortal.service.AuthService;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = Controller.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = { SecurityConfig.class, JwtFilter.class }
        ),
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class,
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /login with valid credentials should return 200 and a token")
    void login_WithValidCredentials_ShouldReturn200AndToken() throws Exception {
        var request = new AuthRequest("john_doe", "password123", "john@example.com");
        var fakeToken = "mocked-jwt-token";
        when(authService.loginUser("john_doe", "password123")).thenReturn(fakeToken);

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value(fakeToken));
    }

    @Test
    @DisplayName("POST /login with blank username should trigger validation and return 400 Bad Request")
    void login_WithBlankUsername_ShouldReturn400BadRequest() throws Exception {
        // 1. Arrange: username is blank ("")
        var invalidRequestPayload = new AuthRequest("", "password123", "john@example.com");
        // 2. Act & Assert: Should fail @NotBlank validation on AuthRequest
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestPayload)))
                .andExpect(status().isBadRequest());
        // Verify the service was never called because validation intercepted the
        // request
        verifyNoInteractions(authService);
    }

}
