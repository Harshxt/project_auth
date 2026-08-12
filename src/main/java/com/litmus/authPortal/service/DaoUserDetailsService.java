package com.litmus.authPortal.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.litmus.authPortal.model.UserPrincipal;
import com.litmus.authPortal.model.Users;
import com.litmus.authPortal.repository.UsersRepository;

@Configuration
public class DaoUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepo;

    public DaoUserDetailsService(UsersRepository repo) {
        usersRepo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return new UserPrincipal(user);

    }

}
