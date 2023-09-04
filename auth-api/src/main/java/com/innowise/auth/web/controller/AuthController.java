package com.innowise.auth.web.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.auth.web.dto.UserRegisterDto;
import com.innowise.auth.security.exception.UserAlreadyExistException;
import com.innowise.auth.domain.model.User;
import com.innowise.auth.web.service.JwtService;
import com.innowise.auth.web.service.RegisterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AuthController {

    final RegisterService registerService;

    final JwtService jwtService;

    final ObjectMapper objectMapper;

    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> register(@RequestBody UserRegisterDto userDto) {
        try {
            //@formatter:off
            registerService.register(User.builder()
                            .username(userDto.getUsername())
                            .password(userDto.getPassword())
                            .role(userDto.getRole())
                            .build()
            );
            //@formatter:on

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (UserAlreadyExistException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @RequestMapping(value = "/validateToken", method = RequestMethod.GET)
    ResponseEntity<Object> validateToken(@NonNull Authentication authentication) {
//        we have filter on this request, that validate token
        return ResponseEntity.ok().build();
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> login(@NonNull Authentication authentication) {
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String username = user.getUsername();
        List<String> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = jwtService.createToken(username, Map.of("roles", roles));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenAsHttpOnlyCookie(token))
                .header(HttpHeaders.SET_COOKIE, rolesAsCookie(roles))
                .header(HttpHeaders.SET_COOKIE, usernameAsCookie(username))
                .build();
    }

    private String usernameAsCookie(String username) {
        return (ResponseCookie.from("username", username)
                .path("/")
                .domain("localhost")
                .maxAge(3600 * 24 * 30)
                .build()).toString();
    }

    private String rolesAsCookie(List<String> roles) {
        String value = roles.stream().collect(Collectors.joining("&"));
        return (ResponseCookie.from("roles", value)
                .path("/")
                .domain("localhost")
                .maxAge(3600 * 24 * 30)
                .build()).toString();
    }


    private String tokenAsHttpOnlyCookie(String token) {
        return (ResponseCookie.from("token", token)
                .path("/")
                .domain("localhost")
                .httpOnly(true)
                .maxAge(7200)
                .build()).toString();
    }


}
