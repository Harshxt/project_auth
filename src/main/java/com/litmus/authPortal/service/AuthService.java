package com.litmus.authPortal.service;

import java.time.LocalDateTime;
import javax.naming.AuthenticationException;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.repository.UsersRepository;

@Configuration
public class AuthService {
    private final JwtService jwtService;
    private final DaoUserDetailsService daoUserDetailsService;
    // private final AuthenticationProvider authenticationProvider;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepo;
    private final AuthenticationManager authManager;

    AuthService(UsersRepository usersRepo, PasswordEncoder passwordEncoder, AuthenticationManager authManager,
            AuthenticationProvider authenticationProvider, DaoUserDetailsService daoUserDetailsService,
            JwtService jwtService) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        // this.authenticationProvider = authenticationProvider;
        this.daoUserDetailsService = daoUserDetailsService;
        this.jwtService = jwtService;
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

    public String loginUser(String username, String password) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

        if (authentication.isAuthenticated()) {

            return jwtService.generateToken(username, password);
        } else
            throw new BadCredentialsException("Invalid username and password");
    }

    public UserDetails getUserDetails(String username, String password) {
        return daoUserDetailsService.loadUserByUsername(username);
    }
}
