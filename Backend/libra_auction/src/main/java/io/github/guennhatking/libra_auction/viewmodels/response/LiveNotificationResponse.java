package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.OffsetDateTime;

public record LiveNotificationResponse(
        String id,
        String auctionId,
        String content,
        OffsetDateTime sentAt) {
}