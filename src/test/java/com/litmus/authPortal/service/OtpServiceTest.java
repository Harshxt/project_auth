package com.litmus.authPortal.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.litmus.authPortal.model.EmailOtp;
import com.litmus.authPortal.repository.EmailOtpRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private EmailOtpRepository otpRepo;

    @InjectMocks
    private OtpService otpService;

    @Test
    @DisplayName("generateOtp should delete existing OTP and save a new 6-digit one")
    void generateOtp_ShouldDeleteOldOtpAndSaveNew() {
        // Arrange
        var email = "user@example.com";
        when(otpRepo.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var generatedOtp = otpService.generateOtp(email);

        // Assert
        assertThat(generatedOtp).isNotNull();
        assertThat(generatedOtp.getEmail()).isEqualTo(email);
        assertThat(generatedOtp.getOtp()).hasSize(6);

        verify(otpRepo).deleteByEmail(email);
        verify(otpRepo).save(any(EmailOtp.class));
    }

    @Test
    @DisplayName("validateOtp should return false when no record exists for email")
    void validateOtp_WhenRecordNotFound_ShouldReturnFalse() {
        // Arrange
        var email = "unknown@example.com";
        when(otpRepo.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        var isValid = otpService.validateOtp(email, "123456");

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateOtp should return true and delete record when OTP matches and is valid")
    void validateOtp_WhenOtpMatches_ShouldReturnTrueAndDelete() {
        // Arrange
        var email = "valid@example.com";
        var otpRecord = new EmailOtp(email, "654321", LocalDateTime.now().plusMinutes(10));
        when(otpRepo.findByEmail(email)).thenReturn(Optional.of(otpRecord));

        // Act
        var isValid = otpService.validateOtp(email, "654321");

        // Assert
        assertThat(isValid).isTrue();
        verify(otpRepo).delete(otpRecord);
    }

    @Test
    @DisplayName("validateOtp should return false and delete record when OTP is expired")
    void validateOtp_WhenOtpExpired_ShouldReturnFalseAndDelete() {
        // Arrange
        var email = "expired@example.com";
        var expiredOtpRecord = new EmailOtp(email, "112233", LocalDateTime.now().minusMinutes(1));
        when(otpRepo.findByEmail(email)).thenReturn(Optional.of(expiredOtpRecord));

        // Act
        var isValid = otpService.validateOtp(email, "112233");

        // Assert
        assertThat(isValid).isFalse();
        verify(otpRepo).delete(expiredOtpRecord);
    }

    @Test
    @DisplayName("validateOtp should increment failed attempts and return false when OTP does not match")
    void validateOtp_WhenOtpMismatches_ShouldIncrementAttemptsAndReturnFalse() {
        // Arrange
        var email = "mismatch@example.com";
        var otpRecord = new EmailOtp(email, "123456", LocalDateTime.now().plusMinutes(10));
        when(otpRepo.findByEmail(email)).thenReturn(Optional.of(otpRecord));

        // Act
        var isValid = otpService.validateOtp(email, "999999");

        // Assert
        assertThat(isValid).isFalse();
        assertThat(otpRecord.getFailedAttempts()).isEqualTo(1);
        verify(otpRepo).save(otpRecord);
    }
}
