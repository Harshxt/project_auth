package com.litmus.authPortal.config.authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.litmus.authPortal.service.DaoUserDetailsService;
import com.litmus.authPortal.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private DaoUserDetailsService userDetailsService;

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal should authenticate user and set SecurityContext when valid Bearer token is provided")
    void doFilterInternal_WhenValidToken_ShouldSetAuthentication() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        var token = "valid.jwt.token";
        var username = "charlie";
        request.addHeader("Authorization", "Bearer " + token);

        UserDetails userDetails = new User(username, "password", Collections.emptyList());

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("charlie");
        assertThat(authentication.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("doFilterInternal should pass through without authenticating when Authorization header is missing")
    void doFilterInternal_WhenNoAuthHeader_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("doFilterInternal should pass through without authenticating when Authorization is not Bearer")
    void doFilterInternal_WhenBasicAuthHeader_ShouldNotAuthenticate() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();
        request.addHeader("Authorization", "Basic user:pass");

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    @DisplayName("doFilterInternal should catch JwtException and continue filter chain without setting auth")
    void doFilterInternal_WhenInvalidJwt_ShouldHandleExceptionGracefully() throws ServletException, IOException {
        // Arrange
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();
        request.addHeader("Authorization", "Bearer malformed.token.here");

        when(jwtService.extractUsername("malformed.token.here")).thenThrow(new JwtException("Invalid token format"));

        // Act
        jwtFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(userDetailsService, never()).loadUserByUsername(any());
    }
}
