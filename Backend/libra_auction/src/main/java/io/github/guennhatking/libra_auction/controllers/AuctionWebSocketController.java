package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.services.AuctionStateRedisService;
import io.github.guennhatking.libra_auction.services.AuctionWebSocketNotificationService;
import io.github.guennhatking.libra_auction.viewmodels.request.BidMessage;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import io.github.guennhatking.libra_auction.models.auction.AuctionLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Controller for handling auction bids
 */
@Controller
public class AuctionWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(AuctionWebSocketController.class);

    private final AuctionRepository auctionRepository;
    private final AuctionStateRedisService auctionStateRedisService;
    private final AuctionWebSocketNotificationService auctionWebSocketNotificationService;

    // In-memory storage for bid history per auction
    private static final Map<String, List<BidResponse>> auctionBids = new ConcurrentHashMap<>();

    // Configuration
    private static final int FINAL_MINUTES_WINDOW = 5;
    private static final int EXTENSION_MINUTES = 5;

    public AuctionWebSocketController(
            AuctionRepository auctionRepository,
            AuctionStateRedisService auctionStateRedisService,
            AuctionWebSocketNotificationService auctionWebSocketNotificationService) {
        this.auctionRepository = auctionRepository;
        this.auctionStateRedisService = auctionStateRedisService;
        this.auctionWebSocketNotificationService = auctionWebSocketNotificationService;
    }

    /**
     * Main bid handler - routes to appropriate auction type handler
     */
    @MessageMapping("/bid")
    public void handleBid(BidMessage bidMessage) {
        logger.info("Received bid message: auctionId={}, bidAmount={}, bidderId={}, bidderName={}",
                bidMessage.auctionId(), bidMessage.bidAmount(), bidMessage.bidderId(), bidMessage.bidderName());
        try {
            // Validate auction exists
            Auction auction = auctionRepository.findById(bidMessage.auctionId())
                    .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

            logger.info("Auction found: id={}, status={}, currentPrice={}, startingPrice={}, minimumBidIncrement={}",
                    auction.getId(), auction.getAuctionStatus(),
                    auction.getCurrentPrice(), auction.getStartingPrice(), auction.getMinimumBidIncrement());

            // Validate auction is active
            if (auction.getAuctionStatus().equals(AuctionStatus.PAUSED)) {
                String errorMsg = "Phiên đấu giá đang tạm dừng. Không thể đặt giá lúc này.";
                logger.error(errorMsg);
                sendErrorNotification(bidMessage.auctionId(), errorMsg);
                return;
            }

            if (!auction.getAuctionStatus().equals(AuctionStatus.IN_PROGRESS)) {
                String errorMsg = "Auction is not active. Current status: " + auction.getAuctionStatus();
                logger.error(errorMsg);
                sendErrorNotification(bidMessage.auctionId(), errorMsg);
                return;
            }

            handleAscendingAuction(bidMessage, auction);

            // After processing bid, check if auction should be extended
            // If a bid is placed within 5 minutes of end time, extend by 5 minutes
            checkAndExtendAuctionIfNeeded(bidMessage.auctionId());

        } catch (IllegalArgumentException e) {
            logger.error("IllegalArgumentException: {}", e.getMessage());
            sendErrorNotification(bidMessage.auctionId(), e.getMessage());
        } catch (Exception e) {
            logger.error("Exception processing bid: {}", e.getMessage(), e);
            sendErrorNotification(bidMessage.auctionId(),
                    "Internal error: " + e.getMessage());
        }
    }

    /**
     * DAU_GIA_LEN (English Auction - Ascending)
     * Rules:
     * - Starting price is startingPrice
     * - Each bid must be at least (currentPrice + minimumBidIncrement)
     * - Highest bidder at the end wins
     * - All bids are visible to participants
     */
    private void handleAscendingAuction(BidMessage bidMessage, Auction auction) {
        long minimumBid = auction.getCurrentPrice() > 0
                ? auction.getCurrentPrice() + auction.getMinimumBidIncrement()
                : auction.getStartingPrice();

        logger.info("Processing ascending auction bid: minimumBid={}, bidAmount={}", minimumBid, bidMessage.bidAmount());

        // Validate bid amount
        if (bidMessage.bidAmount() < minimumBid) {
            String errorMsg = String.format("Bid must be at least %.0f (current price: %.0f + minimum step: %.0f)",
                    minimumBid, auction.getCurrentPrice(), auction.getMinimumBidIncrement());
            logger.error(errorMsg);
            sendErrorNotification(bidMessage.auctionId(), errorMsg);
            return;
        }

        // Create and store bid response
        BidResponse bidResponse = createBidResponse(bidMessage, "SUCCESS");
        recordBid(bidMessage.auctionId(), bidResponse);

        AuctionLog log = new AuctionLog();
        log.setAuction(auction);
        log.setBidAmount(bidMessage.bidAmount());

        if (auction.getBidHistory() == null) {
            auction.setBidHistory(new ArrayList<>());
        }
        auction.getBidHistory().add(log);
        auctionRepository.save(auction);

        logger.info("Bid accepted and saved to log: auctionId={}, bidAmount={}", bidMessage.auctionId(), bidMessage.bidAmount());

        // Broadcast to all participants (bids are visible)
        broadcastBid(bidMessage.auctionId(), bidResponse);
    }

    /**
     * Helper: Create standard bid response
     */
    private BidResponse createBidResponse(BidMessage message, String status) {
        return new BidResponse(
                message.auctionId(),
                message.bidAmount(),
                message.bidderId(),
                message.bidderName(),
                OffsetDateTime.now(ZoneOffset.ofHours(7)),
                status);
    }

    /**
     * Helper: Record bid in history
     */
    private void recordBid(String auctionId, BidResponse bidResponse) {
        auctionBids.computeIfAbsent(auctionId, k -> new ArrayList<>())
                .add(bidResponse);
    }

    /**
     * Helper: Broadcast bid to all auction participants
     */
    private void broadcastBid(String auctionId, BidResponse bidResponse) {
        auctionWebSocketNotificationService.sendBidUpdate(auctionId, bidResponse);
    }

    /**
     * Helper: Create winner message
     */
    private BidResponse createWinnerMessage(BidResponse bidResponse, String message) {
        return new BidResponse(
                bidResponse.auctionId(),
                bidResponse.bidAmount(),
                bidResponse.bidderId(),
                bidResponse.bidderName(),
                OffsetDateTime.now(ZoneOffset.ofHours(7)),
                "WINNER");
    }

    /**
     * Helper: Send error notification
     */
    private void sendErrorNotification(String auctionId, String errorMessage) {
        BidResponse errorResponse = new BidResponse(
                auctionId,
                null,
                null,
                null,
                OffsetDateTime.now(ZoneOffset.ofHours(7)),
                "ERROR");
        auctionWebSocketNotificationService.sendBidUpdate(auctionId, errorResponse);
    }

    /**
     * Get bid history for an auction (for viewing past bids)
     */
    public List<BidResponse> getBidHistory(String auctionId) {
        return auctionBids.getOrDefault(auctionId, new ArrayList<>());
    }

    /**
     * Clear auction data (cleanup after auction ends)
     */
    public void clearAuctionData(String auctionId) {
        auctionBids.remove(auctionId);
    }

    /**
     * Check if auction is within the final minutes and extend if needed
     * If a bid is placed within FINAL_MINUTES_WINDOW (5 minutes), extend by
     * EXTENSION_MINUTES (5 minutes)
     * 
     * @param auctionId The auction ID
     */
    private void checkAndExtendAuctionIfNeeded(String auctionId) {
        try {
            // Check if auction is within final 5 minutes
            if (auctionStateRedisService.isWithinFinalMinutes(auctionId, FINAL_MINUTES_WINDOW)) {
                // Get current end time
                Long currentEndTimeMillis = auctionStateRedisService.getAuctionEndTime(auctionId);
                if (currentEndTimeMillis != null) {
                    // Calculate new end time (5 minutes later)
                    OffsetDateTime currentEndTime = java.time.Instant.ofEpochMilli(currentEndTimeMillis)
                            .atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime();
                    OffsetDateTime newEndTime = currentEndTime.plusMinutes(EXTENSION_MINUTES);

                    // Update Redis with new end time
                    auctionStateRedisService.extendAuctionEnd(auctionId, newEndTime);

                    // Notify all participants about the extension
                    auctionWebSocketNotificationService.sendAuctionExtensionNotification(auctionId, newEndTime);
                }
            }
        } catch (Exception e) {
            // Log error but don't fail the bid
            System.err.println("Error checking/extending auction: " + e.getMessage());
        }
    }
}
