package com.litmus.authPortal.dto.auth.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record getEmailOtpRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
