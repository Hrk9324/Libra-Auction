package io.github.guennhatking.libra_auction.viewmodels.response;

public record AuthenticationResponse(
    String token,
    String refreshToken
) {
}
