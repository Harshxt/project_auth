package com.litmus.authPortal.model;

import java.time.LocalDateTime;

import com.litmus.authPortal.model.enums.AuthProviderIdentity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Users {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    int id;
    String email;
    String password;
    String username;
    int phoneNumber;
    LocalDateTime userCreated;
    LocalDateTime lastModified;
    @Enumerated(EnumType.STRING)
    AuthProviderIdentity authProvider;

}
