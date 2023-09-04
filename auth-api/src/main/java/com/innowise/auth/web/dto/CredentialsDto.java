package com.innowise.auth.web.dto;


import lombok.NonNull;
import lombok.Value;

@Value
public class CredentialsDto {

    @NonNull
    String username;

    @NonNull
    String password;
}
