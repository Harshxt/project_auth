package com.litmus.authPortal.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.litmus.authPortal.model.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findByEmail(String email);

    @Modifying
    @Transactional
    void deleteByEmail(String email);
}
