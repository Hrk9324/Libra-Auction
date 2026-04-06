package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.LocalDateTime;

public record ProductResponse(
    String id,
    String image_src,
    String title,
    long starting_bid,
    int biders,
    LocalDateTime starting_date,
    String href
) {
}
