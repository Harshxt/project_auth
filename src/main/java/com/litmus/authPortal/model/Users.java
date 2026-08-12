package com.litmus.authPortal.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Users {
    @Id
    int id;
    String email;
    String password;
    String username;
    int phoneNumber;
}
