package com.litmus.authPortal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email) {
}
