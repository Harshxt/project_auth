package com.litmus.authPortal.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Table(name = "email_otp")
@Entity
@Getter
@Setter
@NoArgsConstructor

public class EmailOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String otp;
    private LocalDateTime expiryTime;
    private boolean verified;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryTime);
    }

}
