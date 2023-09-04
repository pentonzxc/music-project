package com.innowise.auth.web.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.*;
import java.util.function.Function;


@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtService {

    @Value("${jwt.secret-key}")
    @Nullable
    String envSecretKey;


    @Value("${jwt.expire-period-seconds}")
    long expirePeriodSeconds;

    Key secretKey;

    @PostConstruct
    protected void init() {

        String base64EncodedSecretKey = Base64.getEncoder().encodeToString(envSecretKey.getBytes());
        byte[] keyBytes = Decoders.BASE64.decode(base64EncodedSecretKey);
        secretKey = Keys.hmacShaKeyFor(keyBytes);

//        when bean was initialized, set our source secret key to null
        envSecretKey = null;
    }

    public String createToken(String username, Map<String, Object> claims) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirePeriodSeconds * 1000);

        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims getAllClaimsFromToken(String token) {
        return (Claims) Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parse(token).getBody();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }


    public Date getExpirationDateFromToken(String token) {
        return this.getClaimFromToken(token, Claims::getExpiration);
    }

    public List<String> getRolesFromToken(String token) {
        return this.getClaimFromToken(token, (claims -> (List<String>) claims.get("roles")));
    }


    private boolean isTokenExpired(String token) {
        Date expiration = this.getExpirationDateFromToken(token);
        return expiration.before(new Date(System.currentTimeMillis()));
    }

    public String getUsernameFromToken(String token) {
        return this.getClaimFromToken(token, Claims::getSubject);
    }

    public boolean validateToken(@NonNull String token, @NonNull String username, @NonNull List<String> roles) throws JwtException {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        var usernameFromToken = this.getUsernameFromToken(token);
        var rolesFromToken = this.getRolesFromToken(token);
        return username.equals(usernameFromToken) && roles.equals(rolesFromToken) && !this.isTokenExpired(token);
    }


}
