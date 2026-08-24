package com.litmus.authPortal.controllers;

import com.litmus.authPortal.dto.GenericResponse;
import com.litmus.authPortal.dto.auth.AuthRequest;
import com.litmus.authPortal.dto.auth.AuthResponse;
import com.litmus.authPortal.dto.auth.otp.SendEmailOtpRequest;
import com.litmus.authPortal.dto.auth.otp.VerifyEmailOtpRequest;
import com.litmus.authPortal.exceptions.UserAlreadyExistsException;
import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.service.AuthService;
import jakarta.validation.Valid;
import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    private final AuthService authService;

    public Controller(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/hello")
    public ResponseEntity<GenericResponse<Void>> hello() {
        return ResponseEntity.ok(new GenericResponse<>(true, "Hello worlds"));
    }

    /* authentication endpoints */

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest request) {
        String username = request.username();
        String password = request.password();

        String token = authService.loginUser(username, password);

        return ResponseEntity.ok(new GenericResponse<>(true, "Login successful", new AuthResponse(token)));
    }

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<Users>> registerBasicAuth(@Valid @RequestBody AuthRequest request)
            throws AuthenticationException {
        String username = request.username();
        String password = request.password();
        String email = request.email();
        if (authService.userExists(username)) {
            throw new UserAlreadyExistsException("User already exist!");
        }
        Users user = authService.registerUser(username, email, password);
        user.setPassword(null);
        user.setId(0);

        return ResponseEntity.ok(new GenericResponse<>(true, "User registered successfully", user));
    }

    @PostMapping({ "/auth/email-otp/send", "/auth/getEmailOtp" })
    public ResponseEntity<GenericResponse<Void>> sendEmailOtp(@Valid @RequestBody SendEmailOtpRequest requestPayload) {
        authService.generateOtpForEmail(requestPayload.email());
        return ResponseEntity.ok(new GenericResponse<>(true,
                "If the email is valid, you will receive a verification code. Please check your spam folder."));
    }

    @PostMapping("/auth/verifyEmail")
    public ResponseEntity<GenericResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailOtpRequest requestPayload) {
        boolean validate = authService.verifyEmail(requestPayload.email(), requestPayload.otp());
        if (!validate) {
            return new ResponseEntity<>(
                    new GenericResponse<>(false, "Invalid or expired OTP."),
                    HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(new GenericResponse<>(true, "Email has been successfully verified."));
    }
}
