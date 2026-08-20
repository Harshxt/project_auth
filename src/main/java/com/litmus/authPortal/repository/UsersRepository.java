package com.litmus.authPortal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.litmus.authPortal.model.Users;

public interface UsersRepository extends JpaRepository<Users, Integer> {
    Users findByUsername(String username);

    Users findByEmail(String email);

    boolean existsByUsername(String username);

}
