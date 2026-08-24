package com.litmus.authPortal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class DaoUserDetailsServiceTest {

    @Mock
    private UsersRepository usersRepo;

    @InjectMocks
    private DaoUserDetailsService daoUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername should return UserDetails when user exists")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        // Arrange
        var user = new Users();
        user.setUsername("testuser");
        user.setPassword("hashedPassword");
        when(usersRepo.findByUsername("testuser")).thenReturn(user);

        // Act
        var userDetails = daoUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
    }

    @Test
    @DisplayName("loadUserByUsername should throw UsernameNotFoundException when user is not found")
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(usersRepo.findByUsername("unknown")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> daoUserDetailsService.loadUserByUsername("unknown"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: unknown");
    }
}
