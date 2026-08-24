package com.litmus.authPortal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // We now need to pass a mock secret since the constructor requires it.
        jwtService = new JwtService("TestSecretKeyThatIsLongEnoughForHS256Algorithm");
    }

    @Test
    @DisplayName("generateToken should produce a signed, non-blank JWT")
    void generateToken_ShouldProduceValidToken() {
        // Act
        var token = jwtService.generateToken("john_doe");

        // Assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // Standard header.payload.signature JWT structure
    }

    @Test
    @DisplayName("extractUsername should correctly extract subject from generated token")
    void extractUsername_ShouldReturnCorrectSubject() {
        // Arrange
        var username = "alice_smith";
        var token = jwtService.generateToken(username);

        // Act
        var extracted = jwtService.extractUsername(token);

        // Assert
        assertThat(extracted).isEqualTo(username);
    }

    @Test
    @DisplayName("validateToken should return true when username matches UserDetails")
    void validateToken_WhenMatchingUser_ShouldReturnTrue() {
        // Arrange
        var username = "bob_builder";
        var token = jwtService.generateToken(username);
        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        // Act
        var isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken should return false when username does not match UserDetails")
    void validateToken_WhenMismatchedUser_ShouldReturnFalse() {
        // Arrange
        var token = jwtService.generateToken("bob_builder");
        UserDetails differentUser = new User("charlie", "password", Collections.emptyList());

        // Act
        var isValid = jwtService.validateToken(token, differentUser);

        // Assert
        assertThat(isValid).isFalse();
    }
}
