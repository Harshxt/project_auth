package com.litmus.authPortal.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmus.authPortal.dto.auth.AuthRequest;
import com.litmus.authPortal.dto.auth.otp.SendEmailOtpRequest;
import com.litmus.authPortal.dto.auth.otp.VerifyEmailOtpRequest;
import com.litmus.authPortal.repository.EmailOtpRepository;
import com.litmus.authPortal.repository.UsersRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthPortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private EmailOtpRepository emailOtpRepository;

    // Mock JavaMailSender to prevent external network calls during integration test
    @MockitoBean
    private JavaMailSender javaMailSender;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("End-to-End: Register -> Login -> Request OTP -> Verify Email -> Authenticated Access")
    void fullAuthLifecycle_ShouldSucceed() throws Exception {
        var username = "e2e_user";
        var email = "e2e_user@example.com";
        var password = "StrongPassword123!";

        // 1. Register new user
        var registerRequest = new AuthRequest(username, password, email);
        var registerResult = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        // 2. Login with registered credentials
        var loginRequest = new AuthRequest(username, password, email);
        var loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String jwtToken = loginJson.get("data").get("token").asText();

        // 3. Request Email OTP
        var sendOtpRequest = new SendEmailOtpRequest(email);
        mockMvc.perform(post("/auth/email-otp/send")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sendOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. Retrieve generated OTP from DB
        var otpOptional = emailOtpRepository.findByEmail(email);
        assertThat(otpOptional).isPresent();
        String generatedOtp = otpOptional.get().getOtp();

        // 5. Verify Email with generated OTP
        var verifyOtpRequest = new VerifyEmailOtpRequest(email, generatedOtp);
        mockMvc.perform(post("/auth/verifyEmail")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var updatedUser = usersRepository.findByEmail(email);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.isEmailVerified()).isTrue();

        // 6. Access protected endpoint with Bearer token
        mockMvc.perform(get("/hello")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hello worlds"));

        // 7. Access protected endpoint without token -> Should be 401 Unauthorized
        mockMvc.perform(get("/hello"))
                .andExpect(status().isUnauthorized());
    }
}
