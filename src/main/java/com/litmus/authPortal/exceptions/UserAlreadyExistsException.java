package com.litmus.authPortal.exceptions;

import javax.naming.AuthenticationException;

/**
 * UserAlreadyExistsException
 */
public class UserAlreadyExistsException extends AuthenticationException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
