package io.github.guennhatking.libra_auction.viewmodels.response;

public record AuctionSessionResponse(
    String id,
    String image_src,
    String title,
    long current_bid,
    int bids,
    long time_left,
    String href
) {
}
