package com.litmus.authPortal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.litmus.authPortal.model.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findOtpByEmail(String email);

    void deleteByEmail(String email);
}
