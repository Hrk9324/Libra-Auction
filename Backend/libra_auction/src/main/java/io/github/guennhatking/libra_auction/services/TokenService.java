package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import io.github.guennhatking.libra_auction.enums.auth.TokenType;
import io.github.guennhatking.libra_auction.models.person.Customer;
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
    private final CustomerService customerService;

    public TokenService(JwtRSA jwtRSA, JwtUtils jwtUtils, CustomerService customerService) {
        this.jwtRSA = jwtRSA;
        this.jwtUtils = jwtUtils;
        this.customerService = customerService;
    }

    public JwtResponse generateTokens(Customer user) throws Exception {
        ensureAccountCanReceiveToken(user);
        String roleName = user.getRole() != null ? user.getRole().getName() : null;

        String accessToken = jwtRSA.createToken(user.getId(), roleName, TokenType.ACCESS, accessTokenExpiration);
        String refreshToken = jwtRSA.createToken(user.getId(), roleName, TokenType.REFRESH, refreshTokenExpiration);

        return new JwtResponse(
            accessToken,
            refreshToken,
            accessTokenExpiration,
            refreshTokenExpiration
        );
    }

    public JwtResponse refreshTokens(String refreshToken) throws Exception {
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
        Customer user = customerService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        ensureAccountCanReceiveToken(user);

        String role = jwtRSA.extractClaim(refreshToken, "role");
        String newAccessToken = jwtRSA.createToken(userId, role, TokenType.ACCESS, accessTokenExpiration);
        String newRefreshToken = jwtRSA.createToken(userId, role, TokenType.REFRESH, refreshTokenExpiration);
        return new JwtResponse(newAccessToken, newRefreshToken, accessTokenExpiration, refreshTokenExpiration);
    }

    public boolean validateToken(String token) throws Exception {
        return jwtRSA.verifyToken(token, jwtUtils.getPublicKey());
    }

    public String extractUserId(String token) throws Exception {
        return jwtRSA.extractClaim(token, "sub");
    }

    private void ensureAccountCanReceiveToken(Customer user) {
        if (user.getAccountStatus() == AccountStatus.LOCKED) {
            throw new IllegalArgumentException("Account is locked");
        }
    }
}
