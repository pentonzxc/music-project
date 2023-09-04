package com.innowise.gateway;

public class AuthenticationCookiesAbsentException extends RuntimeException {
    public AuthenticationCookiesAbsentException() {
        super("Provide full authentication schema");
    }
}
