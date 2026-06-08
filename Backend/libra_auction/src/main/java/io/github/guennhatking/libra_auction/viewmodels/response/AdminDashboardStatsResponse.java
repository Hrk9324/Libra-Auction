package io.github.guennhatking.libra_auction.viewmodels.response;

import java.util.List;

import io.github.guennhatking.libra_auction.enums.auction.ApprovalStatus;
import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.enums.account.AccountStatus;

public record AdminDashboardStatsResponse(
        long totalUsers,
        long pendingUsers,
        long activeUsers,
        long lockedUsers,
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
        List<StatusCountResponse<AuctionStatus>> auctionStatusBreakdown,
        List<StatusCountResponse<ApprovalStatus>> approvalStatusBreakdown,
        List<StatusCountResponse<AccountStatus>> userStatusBreakdown,
        List<SellerDashboardStatsResponse.DashboardRevenuePointResponse> revenueLast7Days,
        List<SellerDashboardStatsResponse.DashboardAuctionSummaryResponse> recentAuctions) {

    public record StatusCountResponse<T>(T status, long count) {
    }
}
