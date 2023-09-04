package com.innowise.auth.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.auth.web.service.JwtService;
import com.innowise.auth.web.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.innowise.auth.web.util.CookieUtil.cookieByName;
import static com.innowise.auth.web.util.CookieUtil.getCookieMultiValue;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    final JwtService service;

    final ObjectMapper objectMapper;

    final String TokenValidationResourcePath = "/validateToken";

    final String TokenValidationResourceMethod = HttpMethod.GET.name();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (TokenValidationResourcePath.equals(path) && TokenValidationResourceMethod.equals(method)) {
            log.info("Get cookies from request");
            Cookie tokenCookie = cookieByName(request, "token");
            Cookie usernameCookie = cookieByName(request, "username");
            Cookie rolesCookie = cookieByName(request, "roles");

            List<String> roles = getCookieMultiValue(rolesCookie);
            String token = tokenCookie.getValue();
            String username = usernameCookie.getValue();

            if (service.validateToken(token, username, roles)) {
                log.info("authenticate username - {} , roles - {}", username, roles);
                authenticateThisRequest(username);
                filterChain.doFilter(request, response);
            } else {
                log.info("invalidate token cookie");
                String newEmptyCookie = ResponseCookie.from(tokenCookie.getName()).build().toString();
                response.setHeader(HttpHeaders.SET_COOKIE, newEmptyCookie);
                response.sendError(HttpStatus.FORBIDDEN.value());
            }

        } else {
            filterChain.doFilter(request, response);
        }
    }

    private void authenticateThisRequest(String username) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.NO_AUTHORITIES)
        );
    }
}
