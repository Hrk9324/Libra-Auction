package io.github.guennhatking.libra_auction.viewmodels.response;

public record JwtResponse(
    String token,
    String refreshToken,
    String type,
    long accessTokenExpiration,
    String nguoiDungId
) {
    public JwtResponse(String token, String refreshToken, long accessTokenExpiration, String nguoiDungId) {
        this(token, refreshToken, "Bearer", accessTokenExpiration, nguoiDungId);
    }
}
