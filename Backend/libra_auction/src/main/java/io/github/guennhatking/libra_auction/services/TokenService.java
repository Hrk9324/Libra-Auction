package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.TokenType;
import io.github.guennhatking.libra_auction.security.JwtRSA;
import io.github.guennhatking.libra_auction.security.JwtUtils;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    public JwtResponse generateTokens(String userId) throws Exception {
        String accessToken = jwtRSA.createToken(userId, TokenType.ACCESS, accessTokenExpiration);
        String refreshToken = jwtRSA.createToken(userId, TokenType.REFRESH, refreshTokenExpiration);

        return new JwtResponse(
            accessToken,
            refreshToken,
            accessTokenExpiration / 1000,
            userId
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
        return jwtRSA.createToken(userId, TokenType.ACCESS, accessTokenExpiration);
    }

    public boolean validateToken(String token) throws Exception {
        return jwtRSA.verifyToken(token, jwtUtils.getPublicKey());
    }

    public String extractUserId(String token) throws Exception {
        return jwtRSA.extractClaim(token, "sub");
    }
}
