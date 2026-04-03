package io.github.guennhatking.libra_auction.viewmodels.response;

public record TokenResponse(
        String token,
        String type,
        long accessTokenExpiration) {
    public TokenResponse(String token, long accessTokenExpiration) {
        this(token, "Bearer", accessTokenExpiration);
    }
}
