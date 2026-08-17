package com.litmus.authPortal.dto.auth;

public record AuthResponse(
        String token,
        String message) {
    public AuthResponse(String token) {
        this(token, "Success");
    }
}
