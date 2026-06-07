package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.services.AuctionStateRedisService;
import io.github.guennhatking.libra_auction.services.AuctionWebSocketNotificationService;
import io.github.guennhatking.libra_auction.viewmodels.request.BidMessage;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import io.github.guennhatking.libra_auction.models.auction.AuctionLog;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionParticipationInfoRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

/**
 * WebSocket Controller for handling auction bids
 */
@Controller
public class AuctionWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(AuctionWebSocketController.class);

    private final AuctionRepository auctionRepository;
    private final AuctionStateRedisService auctionStateRedisService;
    private final AuctionWebSocketNotificationService auctionWebSocketNotificationService;
    private final AuctionParticipationInfoRepository participationInfoRepository;
    private final CustomerRepository customerRepository;

    // Configuration
    private static final int FINAL_MINUTES_WINDOW = 5;
    private static final int EXTENSION_MINUTES = 5;

    public AuctionWebSocketController(
            AuctionRepository auctionRepository,
            AuctionStateRedisService auctionStateRedisService,
            AuctionWebSocketNotificationService auctionWebSocketNotificationService,
            AuctionParticipationInfoRepository participationInfoRepository,
            CustomerRepository customerRepository) {
        this.auctionRepository = auctionRepository;
        this.auctionStateRedisService = auctionStateRedisService;
        this.auctionWebSocketNotificationService = auctionWebSocketNotificationService;
        this.participationInfoRepository = participationInfoRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Main bid handler - routes to appropriate auction type handler
     */
    @Transactional
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

            // Validate bidderId is provided
            if (bidMessage.bidderId() == null || bidMessage.bidderId().isBlank()) {
                sendErrorNotification(bidMessage.auctionId(), "Bạn cần đăng nhập để đặt giá.");
                return;
            }

            // Validate auction is active
            if (auction.getAuctionStatus().equals(AuctionStatus.PAUSED)) {
                sendErrorNotification(bidMessage.auctionId(), "Phiên đấu giá đang tạm dừng. Không thể đặt giá lúc này.");
                return;
            }

            if (!auction.getAuctionStatus().equals(AuctionStatus.IN_PROGRESS)) {
                sendErrorNotification(bidMessage.auctionId(), "Phiên đấu giá chưa bắt đầu hoặc đã kết thúc.");
                return;
            }

            // Validate bidder is not the auction creator
            if (auction.getCreator() != null && auction.getCreator().getId().equals(bidMessage.bidderId())) {
                sendErrorNotification(bidMessage.auctionId(), "Người tạo phiên đấu giá không thể đặt giá.");
                return;
            }

            // Validate bidder is not the administrator
            if (customerRepository.findById(bidMessage.bidderId())
                    .map(customer -> customer.getRole().getName().equalsIgnoreCase("ADMIN"))
                    .orElse(false)) {
                sendErrorNotification(bidMessage.auctionId(), "Quản trị viên không thể đặt giá.");
                return;
            }

            // Validate bidder is registered for this auction
            boolean isRegistered = participationInfoRepository
                    .findByParticipantIdAndAuctionId(bidMessage.bidderId(), bidMessage.auctionId())
                    .isPresent();
            if (!isRegistered) {
                sendErrorNotification(bidMessage.auctionId(), "Bạn chưa đăng ký tham gia phiên đấu giá này.");
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

        OffsetDateTime bidTime = OffsetDateTime.now(ZoneOffset.ofHours(7));
        AuctionLog log = new AuctionLog();
        log.setAuction(auction);
        log.setBidAmount(bidMessage.bidAmount());
        log.setTimestamp(bidTime);
        // Set bidder
        customerRepository.findById(bidMessage.bidderId()).ifPresent(log::setBidder);

        if (auction.getBidHistory() == null) {
            auction.setBidHistory(new ArrayList<>());
        }
        auction.getBidHistory().add(log);
        auctionRepository.save(auction);

        logger.info("Bid accepted and saved to log: auctionId={}, bidAmount={}", bidMessage.auctionId(), bidMessage.bidAmount());

        // Broadcast to all participants (bids are visible)
        BidResponse bidResponse = createBidResponse(bidMessage, "SUCCESS", bidTime);
        broadcastBid(bidMessage.auctionId(), bidResponse);
    }

    /**
     * Helper: Create standard bid response
     */
    private BidResponse createBidResponse(BidMessage message, String status) {
        return createBidResponse(message, status, OffsetDateTime.now(ZoneOffset.ofHours(7)));
    }

    private BidResponse createBidResponse(BidMessage message, String status, OffsetDateTime bidTime) {
        return new BidResponse(
                message.auctionId(),
                message.bidAmount(),
                message.bidderId(),
                message.bidderName(),
                bidTime,
                status);
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
                errorMessage,
                OffsetDateTime.now(ZoneOffset.ofHours(7)),
                "ERROR");
        auctionWebSocketNotificationService.sendBidUpdate(auctionId, errorResponse);
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
