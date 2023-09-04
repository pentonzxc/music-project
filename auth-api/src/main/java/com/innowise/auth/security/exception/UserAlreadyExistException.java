package com.innowise.auth.security.exception;

public class UserAlreadyExistException extends RuntimeException {

    public UserAlreadyExistException(String name) {
        super("User with name - " + name + ", already exist");
    }
}
