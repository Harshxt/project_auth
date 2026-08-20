package com.litmus.authPortal.dto.auth.otp;

public record postVerifyEmailRequest(String otp, String email) {

}
