package org.e2e.labe2e04.security.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtService {
    @Value("${jwt.key}")
    private String secret;

    private Algorithm getSigningAlgorithm() {
    }

    public String extractUsername(String token) {
    }

    public String generateToken(UserDetails data) {
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
    }
}
