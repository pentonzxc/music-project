package com.innowise.auth.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {
    READ(Prefix() + "read"),
    WRITE(Prefix() + "write");

    private static String Prefix() {
        return "permission:";
    }


    @Getter
    private final String permission;
}
