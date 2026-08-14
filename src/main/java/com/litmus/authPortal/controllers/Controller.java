package com.litmus.authPortal.controllers;

import com.litmus.authPortal.exceptions.UserAlreadyExistsException;
import com.litmus.authPortal.service.AuthService;
import com.litmus.authPortal.service.JwtService;

import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {
    private final AuthService authService;
    private final JwtService jwtService;

    Controller(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @GetMapping("/hello")

    public String hello() {
        return "Hello worlds";
    }

    /* authentication endpoints */

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> payload) {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");

        if (username == null || password == null) {
            // TODO: Add exception instead and append to controller advice
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        String response = authService.loginUser(username, password);
        String jwtToken = jwtService.generateToken(username, password);

        return ResponseEntity.ok(jwtToken);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerBasicAuth(@RequestBody Map<String, Object> payload)
            throws AuthenticationException {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        if (authService.userExists(username)) {
            throw new UserAlreadyExistsException("User already exist!");
        } else {
            authService.registerUser(username, password);
        }
        return ResponseEntity.ok("Registered");
    }

}
