package com.litmus.authPortal.service;

import java.time.LocalDateTime;

import javax.naming.AuthenticationException;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.repository.UsersRepository;

@Configuration
public class AuthService {
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepo;

    AuthService(UsersRepository usersRepo, PasswordEncoder passwordEncoder) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public Users registerUser(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setUserCreated(LocalDateTime.now());
        user.setLastModified(LocalDateTime.now());

        return usersRepo.save(user);
    }

    public boolean userExists(String username) throws AuthenticationException {
        Users user = usersRepo.findByUsername(username);
        if (user != null) {
            return true;
        }
        return false;
    }
}
