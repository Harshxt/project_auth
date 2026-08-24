package com.litmus.authPortal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.model.enums.AuthProviderIdentity;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private UsersRepository usersRepo;

    @Autowired
    private TestEntityManager entityManager;

    private Users createSampleUser(String username, String email) {
        var user = new Users();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("secretPassword123");
        user.setAuthProvider(AuthProviderIdentity.LOCAL);
        user.setUserCreated(LocalDateTime.now());
        user.setLastModified(LocalDateTime.now());
        user.setEmailVerified(false);
        return user;
    }

    @Test
    @DisplayName("findByUsername should return User entity when username exists")
    void findByUsername_WhenUserExists_ShouldReturnUser() {
        // Arrange
        var user = createSampleUser("alice", "alice@example.com");
        entityManager.persistAndFlush(user);

        // Act
        var foundUser = usersRepo.findByUsername("alice");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("alice");
        assertThat(foundUser.getEmail()).isEqualTo("alice@example.com");
        assertThat(foundUser.getAuthProvider()).isEqualTo(AuthProviderIdentity.LOCAL);
    }

    @Test
    @DisplayName("findByUsername should return null when username does not exist")
    void findByUsername_WhenUserDoesNotExist_ShouldReturnNull() {
        // Act
        var foundUser = usersRepo.findByUsername("nonexistent_user");

        // Assert
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("findByEmail should return User entity when email exists")
    void findByEmail_WhenEmailExists_ShouldReturnUser() {
        // Arrange
        var user = createSampleUser("bob", "bob@example.com");
        entityManager.persistAndFlush(user);

        // Act
        var foundUser = usersRepo.findByEmail("bob@example.com");

        // Assert
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("bob");
        assertThat(foundUser.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    @DisplayName("findByEmail should return null when email does not exist")
    void findByEmail_WhenEmailDoesNotExist_ShouldReturnNull() {
        // Act
        var foundUser = usersRepo.findByEmail("unknown@example.com");

        // Assert
        assertThat(foundUser).isNull();
    }

    @Test
    @DisplayName("existsByUsername should return true when user exists, false otherwise")
    void existsByUsername_ShouldCheckPresenceCorrectly() {
        // Arrange
        var user = createSampleUser("charlie", "charlie@example.com");
        entityManager.persistAndFlush(user);

        // Act & Assert
        assertThat(usersRepo.existsByUsername("charlie")).isTrue();
        assertThat(usersRepo.existsByUsername("david")).isFalse();
    }
}
