package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.enums.product.ProductStatus;
import io.github.guennhatking.libra_auction.models.auction.AuctionResult;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.models.auction.AuctionParticipationInfo;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionResultRepository;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.repositories.product.ProductRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
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
    private final CustomerRepository customerRepository;

    public AuctionStateTransitionService(
            AuctionRepository auctionRepository,
            AuctionResultRepository auctionResultRepository,
            BidHistoryService bidHistoryService,
            SimpMessagingTemplate messagingTemplate,
            EmailNotificationService emailNotificationService,
            AuctionStateRedisService auctionStateRedisService,
            ProductRepository productRepository,
            CustomerRepository customerRepository) {
        this.auctionRepository = auctionRepository;
        this.auctionResultRepository = auctionResultRepository;
        this.bidHistoryService = bidHistoryService;
        this.messagingTemplate = messagingTemplate;
        this.emailNotificationService = emailNotificationService;
        this.auctionStateRedisService = auctionStateRedisService;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
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
            if (auction.getEndTime() == null && auction.getStartTime() != null) {
                auction.setEndTime(auction.getStartTime().plusSeconds(auction.getDuration()));
            }
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

            Long currentEndTimeMs = auctionStateRedisService.getAuctionEndTime(auctionId);
            long nowMs = System.currentTimeMillis();
            long remainingTimeMs = currentEndTimeMs != null ? Math.max(0, currentEndTimeMs - nowMs) : 0;
            auctionStateRedisService.setRemainingTime(auctionId, remainingTimeMs);

            auction.setAuctionStatus(AuctionStatus.PAUSED);
            auctionRepository.save(auction);

            logger.info("Auction {} paused with remainingTimeMs={}", auctionId, remainingTimeMs);
            sendAuctionStatusUpdateWithExtra(auctionId, "PAUSED", null, remainingTimeMs);
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

            Long remainingTimeMs = auctionStateRedisService.getRemainingTime(auctionId);
            long finalEndTimeMs = 0;
            if (remainingTimeMs != null) {
                finalEndTimeMs = System.currentTimeMillis() + remainingTimeMs;
                OffsetDateTime newEndTime = OffsetDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(finalEndTimeMs), ZoneOffset.ofHours(7));
                auctionStateRedisService.extendAuctionEnd(auctionId, newEndTime);
                auction.setEndTime(newEndTime);
                logger.info("Auction {} resumed with remainingTimeMs={}, newEndTime={}", auctionId, remainingTimeMs, finalEndTimeMs);
            } else {
                Long stored = auctionStateRedisService.getAuctionEndTime(auctionId);
                finalEndTimeMs = stored != null ? stored : 0;
            }
            auctionStateRedisService.clearRemainingTime(auctionId);

            auction.setAuctionStatus(AuctionStatus.IN_PROGRESS);
            auctionRepository.save(auction);

            logger.info("Auction {} resumed, sending newEndTime={}", auctionId, finalEndTimeMs);
            sendAuctionStatusUpdateWithExtra(auctionId, "IN_PROGRESS", finalEndTimeMs > 0 ? finalEndTimeMs : null, null);
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

            // Create AuctionResult
            AuctionResult result = new AuctionResult();
            result.setAuction(auction);
            result.setEndTime(OffsetDateTime.now(ZoneOffset.ofHours(7)));

            Customer winner = null;
            if (latestBidResponse != null && latestBidResponse.bidderId() != null) {
                // Find winner Customer from DB
                winner = customerRepository.findById(latestBidResponse.bidderId()).orElse(null);
                if (winner != null) {
                    result.setWinner(winner);
                    result.setWinningPrice(latestBidResponse.bidAmount());
                    logger.info("Auction {} ended with winner: {} ({}), bid: {} VND",
                        auctionId, winner.getFullName(), winner.getId(), latestBidResponse.bidAmount());
                } else {
                    result.setWinningPrice(latestBidResponse.bidAmount());
                    logger.warn("Auction {} winner not found in DB: {}", auctionId, latestBidResponse.bidderId());
                }
            } else {
                logger.info("Auction {} ended with no bids", auctionId);
            }

            AuctionResult savedResult = auctionResultRepository.save(result);
            auction.setAuctionResult(savedResult);

            // Change status to ENDED
            auction.setAuctionStatus(AuctionStatus.ENDED);
            auctionRepository.save(auction);

            // Send email notifications
            try {
                // Notify seller
                emailNotificationService.sendAuctionEndedNotification(auction);

                // Notify winner
                if (winner != null) {
                    emailNotificationService.sendWinnerNotification(auction, winner);
                }

                // Notify all participants (losers)
                if (auction.getParticipants() != null) {
                    for (AuctionParticipationInfo participant : auction.getParticipants()) {
                        Customer bidder = participant.getParticipant();
                        if (bidder != null && (winner == null || !bidder.getId().equals(winner.getId()))) {
                            try {
                                emailNotificationService.sendAuctionLostNotification(auction, bidder);
                            } catch (Exception e) {
                                logger.error("Failed to send lost email to {}", bidder.getId(), e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to send auction end email notifications", e);
            }

            // Send WebSocket notification
            sendAuctionEndStatusUpdate(auctionId, winner, latestBidResponse);

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
        sendAuctionStatusUpdateWithExtra(auctionId, status, null, null);
    }

    private void sendAuctionEndStatusUpdate(String auctionId, Customer winner, BidResponse latestBidResponse) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "STATUS_CHANGE");
            notification.put("auctionId", auctionId);
            notification.put("status", AuctionStatus.ENDED.toString());
            notification.put("timestamp", OffsetDateTime.now(ZoneOffset.ofHours(7)));
            if (winner != null) {
                notification.put("winnerId", winner.getId());
                notification.put("winnerName", winner.getFullName());
            } else if (latestBidResponse != null && latestBidResponse.bidderName() != null) {
                notification.put("winnerId", latestBidResponse.bidderId());
                notification.put("winnerName", latestBidResponse.bidderName());
            }
            if (latestBidResponse != null && latestBidResponse.bidAmount() != null) {
                notification.put("winningPrice", latestBidResponse.bidAmount());
            }

            messagingTemplate.convertAndSend(
                "/topic/auction/" + auctionId + "/status",
                (Object) notification
            );
        } catch (Exception e) {
            logger.error("Failed to send auction end WebSocket notification for auction {}", auctionId, e);
        }
    }

    /**
     * Send auction status update via WebSocket with optional timing fields.
     * @param auctionId The auction ID
     * @param status The new status
     * @param newEndTimeMs Recalculated end time in milliseconds, when available
     * @param remainingTimeMs Frozen remaining time in milliseconds, when paused
     */
    private void sendAuctionStatusUpdateWithExtra(String auctionId, String status, Long newEndTimeMs, Long remainingTimeMs) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "STATUS_CHANGE");
            notification.put("auctionId", auctionId);
            notification.put("status", status);
            notification.put("timestamp", OffsetDateTime.now(ZoneOffset.ofHours(7)));
            if (newEndTimeMs != null) {
                notification.put("newEndTime", newEndTimeMs);
            }
            if (remainingTimeMs != null) {
                notification.put("remainingTime", remainingTimeMs);
            }

            messagingTemplate.convertAndSend(
                "/topic/auction/" + auctionId + "/status",
                (Object) notification
            );
        } catch (Exception e) {
            logger.error("Failed to send WebSocket notification for auction {}", auctionId, e);
        }
    }
}
