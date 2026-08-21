package com.litmus.authPortal.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.litmus.authPortal.model.EmailOtp;
import com.litmus.authPortal.repository.EmailOtpRepository;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int OTP_EXPIRY_MINUTES = 10;

    private final EmailOtpRepository otpRepo;

    public OtpService(EmailOtpRepository otpRepo) {
        this.otpRepo = otpRepo;
    }

    @Transactional
    public EmailOtp generateOtp(String email) {
        otpRepo.deleteByEmail(email);

        String otpCode = generateSecureOtpCode();
        EmailOtp emailOtp = new EmailOtp(
                email,
                otpCode,
                LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)
        );

        return otpRepo.save(emailOtp);
    }

    @Transactional
    public boolean validateOtp(String email, String providedOtp) {
        EmailOtp otpInDb = otpRepo.findByEmail(email).orElse(null);

        if (otpInDb == null) {
            log.warn("OTP validation failed: No OTP record found for email: {}", email);
            return false;
        }

        if (otpInDb.isExpired()) {
            log.warn("OTP validation failed: Expired OTP for email: {}", email);
            otpRepo.delete(otpInDb);
            return false;
        }

        if (otpInDb.hasExceededMaxAttempts()) {
            log.warn("OTP validation failed: Max attempts exceeded for email: {}", email);
            otpRepo.delete(otpInDb);
            return false;
        }

        boolean isMatch = MessageDigest.isEqual(
                otpInDb.getOtp().getBytes(),
                providedOtp.getBytes()
        );

        if (!isMatch) {
            otpInDb.incrementFailedAttempts();
            otpRepo.save(otpInDb);
            log.warn("OTP mismatch for email: {}. Attempts left: {}",
                    email, EmailOtp.MAX_ATTEMPTS - otpInDb.getFailedAttempts());
            return false;
        }

        otpRepo.delete(otpInDb);
        log.info("OTP successfully verified for email: {}", email);
        return true;
    }

    private String generateSecureOtpCode() {
        int number = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }
}
