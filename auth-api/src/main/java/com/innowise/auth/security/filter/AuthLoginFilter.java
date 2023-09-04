package com.innowise.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.auth.web.dto.CredentialsDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class AuthLoginFilter extends OncePerRequestFilter {

    final String LoginResourcePath = "/login";

    final String LoginResourceMethod = HttpMethod.POST.name();

    final AuthenticationProvider authenticationProvider;

    final ObjectMapper objectMapper;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (LoginResourcePath.equals(path) && LoginResourceMethod.equals(method)) {
            CredentialsDto credentials = credentials(request);
            try {
                Authentication authentication = authenticationProvider.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                credentials.getUsername(),
                                credentials.getPassword()
                        )
                );
                if (authentication.isAuthenticated()) {
                    authenticateThisRequest(authentication);
                    filterChain.doFilter(request, response);
                    return;
                }
            } catch (AuthenticationException __) {}
            response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid credentials to login");
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private CredentialsDto credentials(HttpServletRequest request) throws IOException {
        byte[] bytes = request.getInputStream().readAllBytes();
        return objectMapper.readValue(new String(bytes), CredentialsDto.class);
    }

    private void authenticateThisRequest(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


}
