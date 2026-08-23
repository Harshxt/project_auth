package com.litmus.authPortal.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.model.enums.AuthProviderIdentity;
import com.litmus.authPortal.repository.UsersRepository;

@Configuration
public class DataInitializerConfig {

    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    @Value("${SEED_EMAIL}")
    private String SEED_EMAIL;

    DataInitializerConfig(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    CommandLineRunner initUsers(UsersRepository usersRepository, PasswordEncoder PasswordEncoder) {
        return args -> {
            String defaultEmail = SEED_EMAIL;

            if (usersRepository.findByEmail(defaultEmail) == null) {
                Users admin = new Users();
                admin.setUsername("heyllo");
                admin.setEmail(defaultEmail);
                admin.setPassword(passwordEncoder.encode("worlld"));
                admin.setPhoneNumber(1239485594);
                admin.setUserCreated(LocalDateTime.now());
                admin.setLastModified(admin.getUserCreated());
                admin.setAuthProvider(AuthProviderIdentity.LOCAL);

                usersRepository.save(admin);
                System.out.println(">>> Seeding default user done.");
            }
        };
    }
}
