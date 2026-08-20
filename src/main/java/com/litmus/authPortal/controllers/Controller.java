package com.litmus.authPortal.controllers;

import com.litmus.authPortal.dto.GenericResponse;
import com.litmus.authPortal.dto.auth.AuthRequest;
import com.litmus.authPortal.dto.auth.AuthResponse;
import com.litmus.authPortal.dto.auth.otp.getEmailOtpRequest;
import com.litmus.authPortal.dto.auth.otp.postVerifyEmailRequest;
import com.litmus.authPortal.exceptions.UserAlreadyExistsException;
import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.service.AuthService;
import com.litmus.authPortal.service.EmailService;
import com.litmus.authPortal.service.JwtService;
import jakarta.validation.Valid;
import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class Controller {
    private final AuthService authService;

    Controller(AuthService authService, JwtService jwtService, EmailService emailService) {
        this.authService = authService;
    }

    @GetMapping("/hello")

    public String hello() {
        return "Hello worlds";
    }

    /* authentication endpoints */

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        String username = request.username();
        String password = request.password();

        String token = authService.loginUser(username, password);

        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Users> registerBasicAuth(@Valid @RequestBody AuthRequest request)
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

        return ResponseEntity.ok(user);
    }

    @GetMapping("/auth/getEmailOtp")
    public ResponseEntity<?> getEmailOtp(@RequestParam String email) {
        authService.generateOtpForEmail(email);
        return ResponseEntity.ok(new GenericResponse("email",
                "If the email is valid, you will receive an email, check your spam folder"));

    }

    @PostMapping("/auth/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestBody postVerifyEmailRequest requestPayload) {

        boolean validate = authService.verifyEmail(requestPayload.otp(), requestPayload.email());
        if (!validate) {
            return new ResponseEntity<>(new GenericResponse("OtpFailure", "Email has not been verified"),
                    HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(new GenericResponse("OtpSuccess", "Email has been verified"));
    }

}
