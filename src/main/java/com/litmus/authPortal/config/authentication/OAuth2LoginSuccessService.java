package com.litmus.authPortal.config.authentication;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.model.enums.AuthProviderIdentity;
import com.litmus.authPortal.repository.UsersRepository;
import com.litmus.authPortal.service.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessService implements AuthenticationSuccessHandler {
    private final JwtService jwtService;

    private final UsersRepository userRepo;

    OAuth2LoginSuccessService(UsersRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Users user = userRepo.findByEmail(email);
        if (user == null) {
            Users newUser = new Users();
            newUser.setEmail(email);
            newUser.setUsername(name != null ? name : email);
            newUser.setLastModified(LocalDateTime.now());
            newUser.setUserCreated(LocalDateTime.now());
            newUser.setAuthProvider(AuthProviderIdentity.GOOGLE);
            user = userRepo.save(newUser);
        }

        String token = jwtService.generateToken(user.getUsername());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\": \"" + token + "\", \"type\": \"Bearer\"}");

    }

}
