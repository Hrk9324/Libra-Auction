package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.auth.TokenType;
import io.github.guennhatking.libra_auction.models.account.Role;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.security.JwtRSA;
import io.github.guennhatking.libra_auction.security.JwtUtils;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TokenService {
    @Value("${app.jwt.expiration:86400000}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refreshable-duration:604800000}")
    private Long refreshTokenExpiration;

    private final JwtRSA jwtRSA;
    private final JwtUtils jwtUtils;

    public TokenService(JwtRSA jwtRSA, JwtUtils jwtUtils) {
        this.jwtRSA = jwtRSA;
        this.jwtUtils = jwtUtils;
    }

    public JwtResponse generateTokens(Customer user) throws Exception {
        List<String> roleNames = user.getRoles() == null
                ? Collections.emptyList()
                : user.getRoles().stream()
                        .filter(Objects::nonNull)
                        .map(Role::getName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        String accessToken = jwtRSA.createToken(user.getId(), roleNames, TokenType.ACCESS, accessTokenExpiration);
        String refreshToken = jwtRSA.createToken(user.getId(), roleNames, TokenType.REFRESH, refreshTokenExpiration);

        return new JwtResponse(
            accessToken,
            refreshToken, 
            accessTokenExpiration,
            refreshTokenExpiration
        );
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        if (!jwtRSA.verifyToken(refreshToken, jwtUtils.getPublicKey())) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String tokenType = jwtRSA.extractClaim(refreshToken, "type");
        if (tokenType == null || !tokenType.equals(TokenType.REFRESH.toString())) {
            throw new IllegalArgumentException("Token is not a refresh token");
        }

        Long expirationTime = jwtRSA.getExpirationTime(refreshToken);
        if (expirationTime == null || expirationTime < System.currentTimeMillis()) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        String userId = jwtRSA.extractClaim(refreshToken, "sub");
        List<String> roles = parseRolesClaim(jwtRSA.extractClaim(refreshToken, "roles"));
        return jwtRSA.createToken(userId, roles, TokenType.ACCESS, accessTokenExpiration);
    }

    public boolean validateToken(String token) throws Exception {
        return jwtRSA.verifyToken(token, jwtUtils.getPublicKey());
    }

    public String extractUserId(String token) throws Exception {
        return jwtRSA.extractClaim(token, "sub");
    }

    private List<String> parseRolesClaim(String rawRoles) {
        if (rawRoles == null) {
            return Collections.emptyList();
        }

        String cleaned = rawRoles.trim();
        if (cleaned.isEmpty() || "[]".equals(cleaned)) {
            return Collections.emptyList();
        }

        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        if (cleaned.isEmpty()) {
            return Collections.emptyList();
        }

        return java.util.Arrays.stream(cleaned.split(","))
                .map(String::trim)
                .map(role -> role.replace("\"", ""))
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
    }
}
