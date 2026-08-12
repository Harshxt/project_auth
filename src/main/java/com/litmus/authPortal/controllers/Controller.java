package com.litmus.authPortal.controllers;

import com.litmus.authPortal.repository.UsersRepository;
import com.litmus.authPortal.service.AuthService;

import java.util.Map;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class Controller {
    private final AuthService authService;

    Controller(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/hello")

    public String hello() {
        return "Hello worlds";
    }

    /* authentication endpoints */

    @PostMapping("/login")
    public String login() {
        // TODO: Implement login handling
        return "Todo";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerBasicAuth(@RequestBody Map<String, Object> payload)
            throws AuthenticationException {
        String username = (String) payload.get("username");
        String password = (String) payload.get("password");
        if (authService.userExists(username)) {
            throw new AuthenticationException();
        } else {
            authService.registerUser(username, password);
        }
        return ResponseEntity.ok("Registered");
    }

}
