package io.github.guennhatking.libra_auction.viewmodels.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponse(
    @JsonProperty("id_token")
    String idToken,

    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    Long expiresIn
) {
}
