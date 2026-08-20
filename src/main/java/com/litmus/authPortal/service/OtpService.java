package com.litmus.authPortal.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.litmus.authPortal.model.EmailOtp;
import com.litmus.authPortal.repository.EmailOtpRepository;

import jakarta.validation.constraints.Email;

@Service
public class OtpService {
    @Autowired
    EmailOtpRepository otpRepo;

    public EmailOtp generateOtp(String email) {

        if (otpRepo.findOtpByEmail(email).orElseGet(() -> null) != null) {
            otpRepo.deleteByEmail(email);

        }
        EmailOtp otp = newEmailOtp(email);
        otpRepo.save(otp);
        return otp;

    }

    // util
    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }

    private EmailOtp newEmailOtp(String email) {

        EmailOtp newOtp = new EmailOtp();
        newOtp.setOtp(getRandomNumberString());
        newOtp.setEmail(email);
        newOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        return newOtp;

    }

    public boolean validateOtp(String otp, String email) {
        EmailOtp otpInDb = otpRepo.findOtpByEmail(email).orElseGet(() -> null);
        System.out.println(otpInDb);
        if (otpInDb == null) {
            return false;
        }
        if (otpInDb.getExpiryTime().isBefore(LocalDateTime.now())) {
            System.out.println("Deleting otp");
            otpRepo.delete(otpInDb);
            return false;
        }
        if (!otpInDb.getOtp().equals(otp)) {
            System.out.println("otp in db: " + otpInDb);
            System.out.println("Otp provided: " + otp);
            return false;
        } else {
            otpRepo.delete(otpInDb);
            System.out.println("OTP verified");
            return true;
        }
    }
}
