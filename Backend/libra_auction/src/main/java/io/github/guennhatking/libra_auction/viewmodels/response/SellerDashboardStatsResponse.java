package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.auction.ApprovalStatus;
import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;

public record SellerDashboardStatsResponse(
        long totalAuctions,
        long pendingApprovalAuctions,
        long approvedAuctions,
        long rejectedAuctions,
        long liveAuctions,
        long upcomingAuctions,
        long completedAuctions,
        long failedAuctions,
        long totalBids,
        long totalParticipants,
        long totalRevenue,
        List<DashboardRevenuePointResponse> revenueLast7Days,
        List<DashboardAuctionSummaryResponse> recentAuctions) {

    public record DashboardRevenuePointResponse(LocalDate date, long revenue) {
    }

    public record DashboardAuctionSummaryResponse(
            String auctionId,
            String productName,
            AuctionStatus auctionStatus,
            ApprovalStatus approvalStatus,
            OffsetDateTime startTime,
            OffsetDateTime completedAt,
            long currentPrice,
            long totalBids,
            long totalParticipants,
            Long winningPrice) {
    }
}
