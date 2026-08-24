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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmus.authPortal.config.authentication.JwtFilter;
import com.litmus.authPortal.config.authentication.SecurityConfig;
import com.litmus.authPortal.dto.auth.AuthRequest;
import com.litmus.authPortal.dto.auth.otp.SendEmailOtpRequest;
import com.litmus.authPortal.dto.auth.otp.VerifyEmailOtpRequest;
import com.litmus.authPortal.exceptions.advice.GlobalExceptionHandler;
import com.litmus.authPortal.service.AuthService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
public class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    // --- GET /hello ---

    @Test
    @DisplayName("GET /hello should return 200 OK with GenericResponse greeting")
    void hello_ShouldReturn200AndGreeting() throws Exception {
        mockMvc.perform(get("/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Hello worlds"));
    }

    // --- POST /login ---

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
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value(fakeToken));
    }

    @Test
    @DisplayName("POST /login with bad credentials should return 400 Bad Request from GlobalExceptionHandler")
    void login_WithBadCredentials_ShouldReturn400BadRequest() throws Exception {
        var request = new AuthRequest("john_doe", "wrongPassword", "john@example.com");
        when(authService.loginUser("john_doe", "wrongPassword"))
                .thenThrow(new BadCredentialsException("Invalid username and password"));

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username and password"));
    }

    @Test
    @DisplayName("POST /login with blank username should trigger validation and return 400 Bad Request")
    void login_WithBlankUsername_ShouldReturn400BadRequest() throws Exception {
        var invalidRequestPayload = new AuthRequest("", "password123", "john@example.com");

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestPayload)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // --- POST /register ---

    @Test
    @DisplayName("POST /register for new user should return 200 and token")
    void register_WhenNewUser_ShouldReturn200AndToken() throws Exception {
        var request = new AuthRequest("new_user", "password123", "new@example.com");
        when(authService.userExists("new_user")).thenReturn(false);
        when(authService.loginUser("new_user", "password123")).thenReturn("registered-user-jwt");

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").value("registered-user-jwt"));

        verify(authService).registerUser("new_user", "new@example.com", "password123");
    }

    @Test
    @DisplayName("POST /register when user already exists should return 409 Conflict")
    void register_WhenUserAlreadyExists_ShouldReturn409Conflict() throws Exception {
        var request = new AuthRequest("existing_user", "password123", "existing@example.com");
        when(authService.userExists("existing_user")).thenReturn(true);

        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User already exist!"));
    }

    // --- POST /auth/email-otp/send ---

    @Test
    @DisplayName("POST /auth/email-otp/send with valid email should return 200 OK")
    void sendEmailOtp_WithValidEmail_ShouldReturn200() throws Exception {
        var request = new SendEmailOtpRequest("user@example.com");

        mockMvc.perform(post("/auth/email-otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("If the email is valid, you will receive a verification code. Please check your spam folder."));

        verify(authService).generateOtpForEmail("user@example.com");
    }

    @Test
    @DisplayName("POST /auth/email-otp/send with invalid email should return 400 Bad Request")
    void sendEmailOtp_WithInvalidEmail_ShouldReturn400() throws Exception {
        var invalidPayload = "{\"email\":\"not-an-email\"}";

        mockMvc.perform(post("/auth/email-otp/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    // --- POST /auth/verifyEmail ---

    @Test
    @DisplayName("POST /auth/verifyEmail with valid OTP should return 200 OK")
    void verifyEmail_WithValidOtp_ShouldReturn200() throws Exception {
        var request = new VerifyEmailOtpRequest("user@example.com", "123456");
        when(authService.verifyEmail("user@example.com", "123456")).thenReturn(true);

        mockMvc.perform(post("/auth/verifyEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email has been successfully verified."));
    }

    @Test
    @DisplayName("POST /auth/verifyEmail with invalid OTP should return 400 Bad Request")
    void verifyEmail_WithInvalidOtp_ShouldReturn400() throws Exception {
        var request = new VerifyEmailOtpRequest("user@example.com", "000000");
        when(authService.verifyEmail("user@example.com", "000000")).thenReturn(false);

        mockMvc.perform(post("/auth/verifyEmail")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP."));
    }
}
