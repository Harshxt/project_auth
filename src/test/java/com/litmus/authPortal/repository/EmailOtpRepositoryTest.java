package com.litmus.authPortal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import com.litmus.authPortal.model.EmailOtp;

@DataJpaTest
public class EmailOtpRepositoryTest {
    @Autowired
    private EmailOtpRepository otpRepo;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByEmail should retunrn EmailOtp when record exists")
    void findByEmail_WhenExists_ShouldReturnOtp() {
        var email = "test@example.com";
        var otp = new EmailOtp(email, "123456", LocalDateTime.now().plusMinutes(10));
        entityManager.persistAndFlush(otp);

        var found = otpRepo.findByEmail(email);

        assertThat(found).isPresent();

        assertThat(found.get().getEmail()).isEqualTo(email);
        assertThat(found.get().getOtp()).isEqualTo("123456");
    }

    @Test
    @DisplayName("deleteByEmail should remove the record from the database")
    void deleteByEmail_WhenExists_ShouldDeleteRecord() {

        var email = "delete-me@example.com";
        var otp = new EmailOtp(email, "654321", LocalDateTime.now().plusMinutes(10));
        entityManager.persistAndFlush(otp);

        otpRepo.deleteByEmail(email);
        entityManager.flush();
        entityManager.clear();
        var found = otpRepo.findByEmail(email);

        assertThat(found).isEmpty();

    }

}
