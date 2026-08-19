package com.litmus.authPortal.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequest(
        @NotBlank(message = "Username cannot be blank") String username,

        @NotBlank(message = "Password cannot be blank") @Size(min = 6, message = "Password must be atleast 6 characters") String password,
        @NotBlank(message = "Email is required") String email

) {
}
