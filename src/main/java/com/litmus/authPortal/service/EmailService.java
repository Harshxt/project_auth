package com.litmus.authPortal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public boolean sendOtpMail(String recipient, String otp) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(recipient);
            mailMessage.setText("Your OTP is: " + otp + "\nThis code will expire in 10 minutes.");
            mailMessage.setSubject("OTP for authProject");

            javaMailSender.send(mailMessage);
            log.info("OTP email successfully sent to {}", recipient);
            return true;

        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", recipient, e.getMessage());
            return false;
        }
    }
}
