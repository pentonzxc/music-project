package com.innowise.auth.security;


import com.innowise.auth.security.filter.AuthLoginFilter;
import com.innowise.auth.domain.repo.UserRepository;
import com.innowise.auth.security.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    final JwtFilter jwtFilter;

    final AuthLoginFilter authLoginFilter;

    final UserRepository userRepository;

    final PasswordEncoder passwordEncoder;


    @Bean
    SecurityFilterChain webHttpSecurity(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests((exchanges) -> exchanges
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement((config) -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(authLoginFilter, JwtFilter.class)
                .build();

    }
}

