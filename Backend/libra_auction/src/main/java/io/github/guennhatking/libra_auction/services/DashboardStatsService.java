package io.github.guennhatking.libra_auction.services;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import io.github.guennhatking.libra_auction.enums.auction.ApprovalStatus;
import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.auction.AuctionLog;
import io.github.guennhatking.libra_auction.models.auction.AuctionResult;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.AdminDashboardStatsResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.SellerDashboardStatsResponse;

@Service
public class DashboardStatsService {
    private static final int RECENT_AUCTION_LIMIT = 5;
    private static final int REVENUE_DAYS = 7;

    private final AuctionRepository auctionRepository;
    private final CustomerRepository customerRepository;

    public DashboardStatsService(AuctionRepository auctionRepository, CustomerRepository customerRepository) {
        this.auctionRepository = auctionRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public SellerDashboardStatsResponse getSellerStats(String sellerId) {
        List<Auction> auctions = auctionRepository.findAll().stream()
                .filter(auction -> hasCreator(auction, sellerId))
                .toList();

        return new SellerDashboardStatsResponse(
                auctions.size(),
                countByApprovalStatus(auctions, ApprovalStatus.PENDING_APPROVAL),
                countByApprovalStatus(auctions, ApprovalStatus.APPROVED),
                countByApprovalStatus(auctions, ApprovalStatus.REJECTED),
                countByAuctionStatus(auctions, AuctionStatus.IN_PROGRESS),
                countByAuctionStatus(auctions, AuctionStatus.NOT_STARTED),
                countByAuctionStatus(auctions, AuctionStatus.COMPLETED),
                countByAuctionStatus(auctions, AuctionStatus.FAILED),
                totalBids(auctions),
                totalParticipants(auctions),
                totalRevenue(auctions),
                revenueLast7Days(auctions),
                recentAuctions(auctions));
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminStats() {
        List<Auction> auctions = auctionRepository.findAll();
        List<Customer> users = customerRepository.findAll();

        return new AdminDashboardStatsResponse(
                users.size(),
                countUsersByStatus(users, AccountStatus.PENDING),
                countUsersByStatus(users, AccountStatus.ACTIVE),
                countUsersByStatus(users, AccountStatus.LOCKED),
                auctions.size(),
                countByApprovalStatus(auctions, ApprovalStatus.PENDING_APPROVAL),
                countByApprovalStatus(auctions, ApprovalStatus.APPROVED),
                countByApprovalStatus(auctions, ApprovalStatus.REJECTED),
                countByAuctionStatus(auctions, AuctionStatus.IN_PROGRESS),
                countByAuctionStatus(auctions, AuctionStatus.NOT_STARTED),
                countByAuctionStatus(auctions, AuctionStatus.COMPLETED),
                countByAuctionStatus(auctions, AuctionStatus.FAILED),
                totalBids(auctions),
                totalParticipants(auctions),
                totalRevenue(auctions),
                statusCounts(auctions, Auction::getAuctionStatus, AuctionStatus.class),
                statusCounts(auctions, Auction::getApprovalStatus, ApprovalStatus.class),
                userStatusCounts(users),
                revenueLast7Days(auctions),
                recentAuctions(auctions));
    }

    private boolean hasCreator(Auction auction, String sellerId) {
        return auction.getCreator() != null && Objects.equals(auction.getCreator().getId(), sellerId);
    }

    private long countByAuctionStatus(List<Auction> auctions, AuctionStatus status) {
        return auctions.stream()
                .filter(auction -> auction.getAuctionStatus() == status)
                .count();
    }

    private long countByApprovalStatus(List<Auction> auctions, ApprovalStatus status) {
        return auctions.stream()
                .filter(auction -> auction.getApprovalStatus() == status)
                .count();
    }

    private long countUsersByStatus(List<Customer> users, AccountStatus status) {
        return users.stream()
                .filter(user -> user.getAccountStatus() == status)
                .count();
    }

    private long totalBids(List<Auction> auctions) {
        return auctions.stream()
                .map(Auction::getBidHistory)
                .filter(Objects::nonNull)
                .mapToLong(List::size)
                .sum();
    }

    private long totalParticipants(List<Auction> auctions) {
        return auctions.stream()
                .map(Auction::getParticipants)
                .filter(Objects::nonNull)
                .mapToLong(List::size)
                .sum();
    }

    private long totalRevenue(List<Auction> auctions) {
        return auctions.stream()
                .map(this::winningPrice)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();
    }

    private List<SellerDashboardStatsResponse.DashboardRevenuePointResponse> revenueLast7Days(List<Auction> auctions) {
        LocalDate today = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate();
        LocalDate startDate = today.minusDays(REVENUE_DAYS - 1L);
        Map<LocalDate, Long> revenueByDate = auctions.stream()
                .filter(auction -> auction.getAuctionStatus() == AuctionStatus.COMPLETED)
                .filter(auction -> completedDate(auction) != null)
                .filter(auction -> !completedDate(auction).isBefore(startDate))
                .filter(auction -> !completedDate(auction).isAfter(today))
                .collect(Collectors.groupingBy(
                        this::completedDate,
                        Collectors.summingLong(auction -> {
                            Long price = winningPrice(auction);
                            return price != null ? price : 0L;
                        })));

        List<SellerDashboardStatsResponse.DashboardRevenuePointResponse> points = new ArrayList<>();
        for (int index = 0; index < REVENUE_DAYS; index++) {
            LocalDate date = startDate.plusDays(index);
            points.add(new SellerDashboardStatsResponse.DashboardRevenuePointResponse(
                    date,
                    revenueByDate.getOrDefault(date, 0L)));
        }
        return points;
    }

    private LocalDate completedDate(Auction auction) {
        OffsetDateTime completedAt = auction.getCompletedAt();
        if (completedAt != null) {
            return completedAt.toLocalDate();
        }
        AuctionResult result = auction.getAuctionResult();
        if (result != null && result.getEndTime() != null) {
            return result.getEndTime().toLocalDate();
        }
        return null;
    }

    private List<SellerDashboardStatsResponse.DashboardAuctionSummaryResponse> recentAuctions(List<Auction> auctions) {
        return auctions.stream()
                .sorted(Comparator.comparing(this::recentTimestamp, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(RECENT_AUCTION_LIMIT)
                .map(this::toAuctionSummary)
                .toList();
    }

    private OffsetDateTime recentTimestamp(Auction auction) {
        if (auction.getCreatedAt() != null) {
            return auction.getCreatedAt();
        }
        if (auction.getStartTime() != null) {
            return auction.getStartTime();
        }
        return auction.getCompletedAt();
    }

    private SellerDashboardStatsResponse.DashboardAuctionSummaryResponse toAuctionSummary(Auction auction) {
        List<AuctionLog> bidHistory = auction.getBidHistory();
        int bidCount = bidHistory != null ? bidHistory.size() : 0;
        int participantCount = auction.getParticipants() != null ? auction.getParticipants().size() : 0;
        String productName = auction.getProduct() != null ? auction.getProduct().getName() : "Unknown product";

        return new SellerDashboardStatsResponse.DashboardAuctionSummaryResponse(
                auction.getId(),
                productName,
                auction.getAuctionStatus(),
                auction.getApprovalStatus(),
                auction.getStartTime(),
                auction.getCompletedAt(),
                currentPrice(auction),
                bidCount,
                participantCount,
                winningPrice(auction));
    }

    private long currentPrice(Auction auction) {
        long currentPrice = auction.getCurrentPrice();
        return currentPrice > 0 ? currentPrice : auction.getStartingPrice();
    }

    private Long winningPrice(Auction auction) {
        AuctionResult result = auction.getAuctionResult();
        if (result == null) {
            return null;
        }
        return result.getWinningPrice();
    }

    private <T extends Enum<T>> List<AdminDashboardStatsResponse.StatusCountResponse<T>> statusCounts(
            List<Auction> auctions,
            Function<Auction, T> statusGetter,
            Class<T> statusType) {
        Map<T, Long> counts = new EnumMap<>(statusType);
        for (T status : statusType.getEnumConstants()) {
            counts.put(status, 0L);
        }
        auctions.stream()
                .map(statusGetter)
                .filter(Objects::nonNull)
                .forEach(status -> counts.put(status, counts.getOrDefault(status, 0L) + 1));

        return counts.entrySet().stream()
                .map(entry -> new AdminDashboardStatsResponse.StatusCountResponse<>(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<AdminDashboardStatsResponse.StatusCountResponse<AccountStatus>> userStatusCounts(List<Customer> users) {
        Map<AccountStatus, Long> counts = new EnumMap<>(AccountStatus.class);
        for (AccountStatus status : AccountStatus.values()) {
            counts.put(status, 0L);
        }
        users.stream()
                .map(Customer::getAccountStatus)
                .filter(Objects::nonNull)
                .forEach(status -> counts.put(status, counts.getOrDefault(status, 0L) + 1));

        return counts.entrySet().stream()
                .map(entry -> new AdminDashboardStatsResponse.StatusCountResponse<>(entry.getKey(), entry.getValue()))
                .toList();
    }
}
