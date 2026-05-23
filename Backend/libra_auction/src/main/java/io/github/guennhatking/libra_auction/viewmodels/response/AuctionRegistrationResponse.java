package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.OffsetDateTime;

public record AuctionRegistrationResponse(
    String id,
    String userId,
    String email,
    String auctionId,
    OffsetDateTime registrationTime
) {
}
