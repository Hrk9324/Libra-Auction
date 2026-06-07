package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.OffsetDateTime;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;

public record AuctionResponse(
        String category_id,
        String category_name,
        String auction_id,
        String auction_name,
        AuctionStatus auction_status,
        String approval_status,
        OffsetDateTime start_time,
        OffsetDateTime end_time,
        Long duration,

        Long starting_price,
        Long current_price,
        Long deposit_amount,
        Long min_bid_increment,

        String product_id,
        String product_name,
        Integer quantity,
        String description,

        List<String> images,
        List<AttributeResponse> attributes,

        Integer total_bids,
        Integer total_participants,

        String failure_reason,
        OffsetDateTime completed_at,

        String creator_id,

        String winner_id,
        String winner_name,
        Long winning_price,

        Long remaining_time) {
}
