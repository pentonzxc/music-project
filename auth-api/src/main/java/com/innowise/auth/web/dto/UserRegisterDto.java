package com.innowise.auth.web.dto;

import com.innowise.auth.domain.model.Role;
import lombok.NonNull;
import lombok.Value;


@Value
public class UserRegisterDto {
    @NonNull
    String username;

    @NonNull
    String password;

    Role role;
}
