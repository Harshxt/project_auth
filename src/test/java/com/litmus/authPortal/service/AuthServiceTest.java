package com.litmus.authPortal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import javax.naming.AuthenticationException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.litmus.authPortal.model.EmailOtp;
import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.model.enums.AuthProviderIdentity;
import com.litmus.authPortal.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsersRepository usersRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Mock
    private DaoUserDetailsService daoUserDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("registerUser should encode password, set LOCAL provider, and persist user")
    void registerUser_ShouldHashPasswordAndSaveUser() {
        // Arrange
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(usersRepo.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var registered = authService.registerUser("john_doe", "john@example.com", "plainPassword");

        // Assert
        assertThat(registered).isNotNull();
        assertThat(registered.getUsername()).isEqualTo("john_doe");
        assertThat(registered.getEmail()).isEqualTo("john@example.com");
        assertThat(registered.getPassword()).isEqualTo("encodedPassword");
        assertThat(registered.getAuthProvider()).isEqualTo(AuthProviderIdentity.LOCAL);
        verify(usersRepo).save(any(Users.class));
    }

    @Test
    @DisplayName("userExists should return true when user is present in repository")
    void userExists_WhenUserFound_ShouldReturnTrue() throws AuthenticationException {
        // Arrange
        when(usersRepo.findByUsername("existing_user")).thenReturn(new Users());

        // Act & Assert
        assertThat(authService.userExists("existing_user")).isTrue();
    }

    @Test
    @DisplayName("userExists should return false when user is not present in repository")
    void userExists_WhenUserNotFound_ShouldReturnFalse() throws AuthenticationException {
        // Arrange
        when(usersRepo.findByUsername("missing_user")).thenReturn(null);

        // Act & Assert
        assertThat(authService.userExists("missing_user")).isFalse();
    }

    @Test
    @DisplayName("loginUser should return JWT token when credentials are valid")
    void loginUser_WithValidCredentials_ShouldReturnJwtToken() {
        // Arrange
        var authToken = new UsernamePasswordAuthenticationToken("john_doe", "password123");
        Authentication authenticated = mock(Authentication.class);
        when(authenticated.isAuthenticated()).thenReturn(true);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticated);
        when(jwtService.generateToken("john_doe")).thenReturn("mock-jwt-token");

        // Act
        var token = authService.loginUser("john_doe", "password123");

        // Assert
        assertThat(token).isEqualTo("mock-jwt-token");
    }

    @Test
    @DisplayName("loginUser should throw BadCredentialsException when authentication is not authenticated")
    void loginUser_WhenNotAuthenticated_ShouldThrowBadCredentialsException() {
        // Arrange
        Authentication unauthenticated = mock(Authentication.class);
        when(unauthenticated.isAuthenticated()).thenReturn(false);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(unauthenticated);

        // Act & Assert
        assertThatThrownBy(() -> authService.loginUser("john_doe", "wrongPassword"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username and password");
    }

    @Test
    @DisplayName("generateOtpForEmail unauthenticated flow: sends email if user exists with that email")
    void generateOtpForEmail_Unauthenticated_WhenUserExists_ShouldSendOtp() {
        // Arrange: unauthenticated (SecurityContext is empty)
        var user = new Users();
        user.setEmail("user@example.com");
        when(usersRepo.findByEmail("user@example.com")).thenReturn(user);

        var generatedOtp = new EmailOtp("user@example.com", "123456", LocalDateTime.now().plusMinutes(10));
        when(otpService.generateOtp("user@example.com")).thenReturn(generatedOtp);

        // Act
        authService.generateOtpForEmail("user@example.com");

        // Assert
        verify(otpService).generateOtp("user@example.com");
        verify(emailService).sendOtpMail("user@example.com", "123456");
    }

    @Test
    @DisplayName("generateOtpForEmail unauthenticated flow: does nothing if user does not exist")
    void generateOtpForEmail_Unauthenticated_WhenUserDoesNotExist_ShouldNotSendOtp() {
        // Arrange
        when(usersRepo.findByEmail("unknown@example.com")).thenReturn(null);

        // Act
        authService.generateOtpForEmail("unknown@example.com");

        // Assert
        verify(otpService, never()).generateOtp(any());
        verify(emailService, never()).sendOtpMail(any(), any());
    }

    @Test
    @DisplayName("generateOtpForEmail authenticated flow: sends OTP when email matches authenticated user")
    void generateOtpForEmail_Authenticated_WhenEmailMatches_ShouldSendOtp() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("alice");

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        var user = new Users();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        when(usersRepo.findByUsername("alice")).thenReturn(user);

        var generatedOtp = new EmailOtp("alice@example.com", "654321", LocalDateTime.now().plusMinutes(10));
        when(otpService.generateOtp("alice@example.com")).thenReturn(generatedOtp);

        // Act
        authService.generateOtpForEmail("alice@example.com");

        // Assert
        verify(otpService).generateOtp("alice@example.com");
        verify(emailService).sendOtpMail("alice@example.com", "654321");
    }

    @Test
    @DisplayName("verifyEmail should mark user as verified when OTP is valid")
    void verifyEmail_WhenOtpIsValid_ShouldMarkUserVerified() {
        // Arrange
        var email = "verify@example.com";
        var user = new Users();
        user.setEmail(email);
        user.setEmailVerified(false);

        when(otpService.validateOtp(email, "123456")).thenReturn(true);
        when(usersRepo.findByEmail(email)).thenReturn(user);

        // Act
        var result = authService.verifyEmail(email, "123456");

        // Assert
        assertThat(result).isTrue();
        assertThat(user.isEmailVerified()).isTrue();
        verify(usersRepo).save(user);
    }

    @Test
    @DisplayName("verifyEmail should return false and not update user when OTP is invalid")
    void verifyEmail_WhenOtpIsInvalid_ShouldReturnFalse() {
        // Arrange
        var email = "verify@example.com";
        when(otpService.validateOtp(email, "000000")).thenReturn(false);

        // Act
        var result = authService.verifyEmail(email, "000000");

        // Assert
        assertThat(result).isFalse();
        verify(usersRepo, never()).save(any());
    }
}
