package com.litmus.authPortal.service;

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
    private EmailOtpRepository otpRepo; // The simulated dependency

    @InjectMocks
    private OtpService otpService; // The real service under test

    @Test
    @DisplayName("generateOtp should delete existing OTP and save a new one")
    void generateOtp_ShouldDeleteOldOtpAndSaveNew() {
        // 1. Arrange (Given)
        var email = "user@example.com";
        when(otpRepo.save(any(EmailOtp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. Act (When)
        var generatedOtp = otpService.generateOtp(email);

        // 3. Assert (Then)
        assertThat(generatedOtp).isNotNull();
        assertThat(generatedOtp.getEmail()).isEqualTo(email);
        assertThat(generatedOtp.getOtp()).isNotBlank();

        // Verify interactions with the mock
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
}
