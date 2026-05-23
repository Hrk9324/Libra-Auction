package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.NotBlank;

public record AuctionRegistrationCreateRequest(
        @NotBlank(message = "auctionId is required") String auctionId) {
}
