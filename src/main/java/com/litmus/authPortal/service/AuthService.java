package com.litmus.authPortal.service;

import java.time.LocalDateTime;
import javax.naming.AuthenticationException;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.litmus.authPortal.model.EmailOtp;
import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.model.enums.AuthProviderIdentity;
import com.litmus.authPortal.repository.UsersRepository;

@Service
public class AuthService {
    private final EmailService emailService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final DaoUserDetailsService daoUserDetailsService;

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepo;
    private final AuthenticationManager authManager;

    AuthService(UsersRepository usersRepo, PasswordEncoder passwordEncoder, AuthenticationManager authManager,
            AuthenticationProvider authenticationProvider, DaoUserDetailsService daoUserDetailsService,
            JwtService jwtService, OtpService otpService, EmailService emailService) {
        this.usersRepo = usersRepo;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;

        this.daoUserDetailsService = daoUserDetailsService;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.emailService = emailService;
    }

    public Users registerUser(String username, String email, String password) {
        String encodedPassword = passwordEncoder.encode(password);

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setUserCreated(LocalDateTime.now());
        user.setLastModified(LocalDateTime.now());
        user.setAuthProvider(AuthProviderIdentity.LOCAL);

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

            return jwtService.generateToken(username);
        } else
            throw new BadCredentialsException("Invalid username and password");
    }

    public UserDetails getUserDetails(String username, String password) {
        return daoUserDetailsService.loadUserByUsername(username);
    }

    public void verifyEmail(String email) {
        Users user = usersRepo.findByEmail(email);
        // if email is invalid or doesn't exists
        if (user == null)
            return;

        EmailOtp otp = otpService.generateOtp(email);
        // if otp not generated
        if (otp == null) {
            return;
        }
        emailService.sendOtpMail(email, otp.getOtp());

        return;
    }

}
