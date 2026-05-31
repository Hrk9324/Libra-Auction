package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.enums.product.ProductStatus;
import io.github.guennhatking.libra_auction.models.auction.AuctionResult;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.auction.AuctionParticipationInfo;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionResultRepository;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.product.ProductRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Service to handle auction state transitions
 * Manages transitions from NOT_STARTED -> IN_PROGRESS -> ENDED
 */
@Service
public class AuctionStateTransitionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionStateTransitionService.class);

    private final AuctionRepository auctionRepository;
    private final AuctionResultRepository auctionResultRepository;
    private final BidHistoryService bidHistoryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final EmailNotificationService emailNotificationService;
    private final AuctionStateRedisService auctionStateRedisService;
    private final ProductRepository productRepository;

    public AuctionStateTransitionService(
            AuctionRepository auctionRepository,
            AuctionResultRepository auctionResultRepository,
            BidHistoryService bidHistoryService,
            SimpMessagingTemplate messagingTemplate,
            EmailNotificationService emailNotificationService,
            AuctionStateRedisService auctionStateRedisService,
            ProductRepository productRepository) {
        this.auctionRepository = auctionRepository;
        this.auctionResultRepository = auctionResultRepository;
        this.bidHistoryService = bidHistoryService;
        this.messagingTemplate = messagingTemplate;
        this.emailNotificationService = emailNotificationService;
        this.auctionStateRedisService = auctionStateRedisService;
        this.productRepository = productRepository;
    }

    /**
     * Transition an auction to STARTED state (IN_PROGRESS)
     * - Send start notifications to participants
     * - Change status in database
     * @param auctionId The auction ID
     */
    @Transactional
    public void startAuction(String auctionId) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (auctionOpt.isEmpty()) {
                logger.warn("Auction not found: {}", auctionId);
                return;
            }

            Auction auction = auctionOpt.get();

            // Only transition if currently in NOT_STARTED state
            if (auction.getAuctionStatus() != AuctionStatus.NOT_STARTED) {
                logger.warn("Auction {} is not in NOT_STARTED state, current: {}",
                    auctionId, auction.getAuctionStatus());
                return;
            }

            // Change status to IN_PROGRESS
            auction.setAuctionStatus(AuctionStatus.IN_PROGRESS);
            auctionRepository.save(auction);

            logger.info("Auction {} started", auctionId);

            // Send email notification
            try {
                emailNotificationService.sendAuctionStartedNotification(auction);
            } catch (Exception e) {
                logger.error("Failed to send auction start email notification", e);
            }

            // Send WebSocket notification
            sendAuctionStatusUpdate(auctionId, AuctionStatus.IN_PROGRESS.toString());

        } catch (Exception e) {
            logger.error("Error starting auction {}: {}", auctionId, e.getMessage(), e);
        }
    }

    /**
     * Pause an auction (IN_PROGRESS -> PAUSED)
     * @param auctionId The auction ID
     */
    @Transactional
    public void pauseAuction(String auctionId) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (auctionOpt.isEmpty()) {
                logger.warn("Auction not found: {}", auctionId);
                return;
            }

            Auction auction = auctionOpt.get();

            if (auction.getAuctionStatus() != AuctionStatus.IN_PROGRESS) {
                logger.warn("Auction {} cannot be paused, current status: {}", auctionId, auction.getAuctionStatus());
                return;
            }

            // Record pause start time in Redis
            auctionStateRedisService.setPaused(auctionId, OffsetDateTime.now(ZoneOffset.ofHours(7)));

            auction.setAuctionStatus(AuctionStatus.PAUSED);
            auctionRepository.save(auction);

            logger.info("Auction {} paused", auctionId);
            sendAuctionStatusUpdate(auctionId, "PAUSED");
        } catch (Exception e) {
            logger.error("Error pausing auction {}: {}", auctionId, e.getMessage(), e);
        }
    }

    /**
     * Resume a paused auction (PAUSED -> IN_PROGRESS)
     * @param auctionId The auction ID
     */
    @Transactional
    public void resumeAuction(String auctionId) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (auctionOpt.isEmpty()) {
                logger.warn("Auction not found: {}", auctionId);
                return;
            }

            Auction auction = auctionOpt.get();

            if (auction.getAuctionStatus() != AuctionStatus.PAUSED) {
                logger.warn("Auction {} cannot be resumed, current status: {}", auctionId, auction.getAuctionStatus());
                return;
            }

            // Calculate paused duration and extend end time
            Long pauseStartMs = auctionStateRedisService.getPauseStartTime(auctionId);
            if (pauseStartMs != null) {
                long pausedDurationMs = System.currentTimeMillis() - pauseStartMs;
                Long currentEndTimeMs = auctionStateRedisService.getAuctionEndTime(auctionId);
                if (currentEndTimeMs != null) {
                    long newEndTimeMs = currentEndTimeMs + pausedDurationMs;
                    OffsetDateTime newEndTime = OffsetDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(newEndTimeMs), ZoneOffset.ofHours(7));
                    auctionStateRedisService.extendAuctionEnd(auctionId, newEndTime);
                    logger.info("Auction {} resumed, extended end time by {} ms", auctionId, pausedDurationMs);
                }
            }
            auctionStateRedisService.clearPaused(auctionId);

            auction.setAuctionStatus(AuctionStatus.IN_PROGRESS);
            auctionRepository.save(auction);

            logger.info("Auction {} resumed", auctionId);
            // Get new end time and send to frontend
            Long newEndTimeMs = auctionStateRedisService.getAuctionEndTime(auctionId);
            String endTimeField = newEndTimeMs != null ? ",\"newEndTime\":" + newEndTimeMs : "";
            sendAuctionStatusUpdateWithExtra(auctionId, "IN_PROGRESS", endTimeField);
        } catch (Exception e) {
            logger.error("Error resuming auction {}: {}", auctionId, e.getMessage(), e);
        }
    }

    /**
     * Transition an auction to ENDED state (ENDED)
     * - Find the winner
     * - Create AuctionResult (auction result)
     * - Send notification emails to winner and auction creator
     * - Change status in database
     * @param auctionId The auction ID
     */
    @Transactional
    public void endAuction(String auctionId) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (auctionOpt.isEmpty()) {
                logger.warn("Auction not found: {}", auctionId);
                return;
            }

            Auction auction = auctionOpt.get();

            // Only transition if currently in IN_PROGRESS or PAUSED state
            if (auction.getAuctionStatus() != AuctionStatus.IN_PROGRESS
                    && auction.getAuctionStatus() != AuctionStatus.PAUSED) {
                logger.warn("Auction {} cannot be ended, current status: {}",
                    auctionId, auction.getAuctionStatus());
                return;
            }

            // Find the winner (latest bid from history)
            BidResponse latestBidResponse = bidHistoryService.getLatestBid(auctionId);

            // Create AuctionResult (Auction Result)
            AuctionResult result = new AuctionResult();
            result.setAuction(auction);
            result.setEndTime(OffsetDateTime.now(ZoneOffset.ofHours(7)));

            if (latestBidResponse != null) {
                // There is a winner - set the winner and price
                // Note: We're using the BidResponse, not database record
                // In production, you should query Customer by email from latestBidResponse.bidderId
                result.setWinningPrice(latestBidResponse.bidAmount());

                logger.info("Auction {} ended with winner bid: {} VND",
                    auctionId, latestBidResponse.bidAmount());
            } else {
                // No bids placed - auction ends with no winner
                logger.info("Auction {} ended with no bids", auctionId);
            }

            AuctionResult savedResult = auctionResultRepository.save(result);
            auction.setAuctionResult(savedResult);

            // Change status to ENDED
            auction.setAuctionStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);

            // Send email notifications
            try {
                if (latestBidResponse != null) {
                    // Send winner notification (to bidder email)
                    logger.info("Sending winner notification to {}", latestBidResponse.bidderId());
                }
                emailNotificationService.sendAuctionEndedNotification(auction);
            } catch (Exception e) {
                logger.error("Failed to send auction end email notifications", e);
            }

            // Send WebSocket notification
            sendAuctionStatusUpdate(auctionId, AuctionStatus.ENDED.toString());

        } catch (Exception e) {
            logger.error("Error ending auction {}: {}", auctionId, e.getMessage(), e);
        }
    }

    /**
     * Cancel an auction (only NOT_STARTED can be cancelled)
     * - Set product back to AVAILABLE
     * - Notify all registered participants and the seller
     * @param auctionId The auction ID
     * @param reason The cancellation reason
     */
    @Transactional
    public void cancelAuction(String auctionId, String reason) {
        try {
            Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
            if (auctionOpt.isEmpty()) {
                logger.warn("Auction not found: {}", auctionId);
                return;
            }

            Auction auction = auctionOpt.get();

            if (auction.getAuctionStatus() != AuctionStatus.NOT_STARTED) {
                throw new IllegalStateException("Chỉ có thể hủy phiên đấu giá chưa bắt đầu. Trạng thái hiện tại: " + auction.getAuctionStatus());
            }

            // Set product back to AVAILABLE
            if (auction.getProduct() != null) {
                auction.getProduct().setStatus(ProductStatus.AVAILABLE);
                productRepository.save(auction.getProduct());
            }

            auction.setAuctionStatus(AuctionStatus.CANCELLED);
            auction.setFailureReason(reason);
            auctionRepository.save(auction);

            // Clean up Redis scheduling
            auctionStateRedisService.cleanupAuction(auctionId);

            // Notify seller
            if (auction.getCreator() != null && auction.getCreator().getEmail() != null) {
                try {
                    emailNotificationService.sendAuctionCancelledNotification(auction, reason);
                } catch (Exception e) {
                    logger.error("Failed to send cancel email to seller", e);
                }
            }

            // Notify all registered participants
            if (auction.getParticipants() != null) {
                for (AuctionParticipationInfo participant : auction.getParticipants()) {
                    Customer bidder = participant.getParticipant();
                    if (bidder != null && bidder.getEmail() != null) {
                        try {
                            emailNotificationService.sendAuctionCancelledNotification(auction, reason);
                        } catch (Exception e) {
                            logger.error("Failed to send cancel email to participant {}", bidder.getId(), e);
                        }
                    }
                }
            }

            // Send WebSocket notification
            sendAuctionStatusUpdate(auctionId, "CANCELLED");

            logger.info("Auction {} cancelled. Reason: {}", auctionId, reason);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error cancelling auction {}: {}", auctionId, e.getMessage(), e);
        }
    }

    /**
     * Send auction status update via WebSocket
     * @param auctionId The auction ID
     * @param status The new status
     */
    private void sendAuctionStatusUpdate(String auctionId, String status) {
        sendAuctionStatusUpdateWithExtra(auctionId, status, "");
    }

    /**
     * Send auction status update via WebSocket with extra JSON fields
     * @param auctionId The auction ID
     * @param status The new status
     * @param extraFields Additional JSON fields (e.g., ",\"newEndTime\":12345")
     */
    private void sendAuctionStatusUpdateWithExtra(String auctionId, String status, String extraFields) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/auction/" + auctionId + "/status",
                "{\"auctionId\":\"" + auctionId + "\",\"status\":\"" + status + "\",\"timestamp\":\"" + OffsetDateTime.now(ZoneOffset.ofHours(7)) + "\"" + extraFields + "}"
            );
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification for auction {}", auctionId, e);
        }
    }
}
