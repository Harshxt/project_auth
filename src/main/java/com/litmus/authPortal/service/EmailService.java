package com.litmus.authPortal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;

    EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String sendOtpMail(String recipient, String otp) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom(sender);
            mailMessage.setTo(recipient);
            mailMessage.setText(otp);
            mailMessage.setSubject("OTP for authProject");

            javaMailSender.send(mailMessage);

            return "Email Sent Successfully to " + recipient + "with body" + otp;

        } catch (Exception e) {
            return "Something went wrong" + e.getMessage() + "for sender" + sender;
        }
    }
}
