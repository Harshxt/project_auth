package com.litmus.authPortal.exceptions;

import org.springframework.security.core.AuthenticationException;

/**
 * UserAlreadyExistsException
 */
public class UserAlreadyExistsException extends AuthenticationException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
