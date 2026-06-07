package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import io.github.guennhatking.libra_auction.models.notification.LiveNotification;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.repositories.notification.LiveNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Service to send real-time WebSocket notifications for auction events
 */
@Service
public class AuctionWebSocketNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionWebSocketNotificationService.class);
    
    private final SimpMessagingTemplate messagingTemplate;
    private final AuctionRepository auctionRepository;
    private final LiveNotificationRepository liveNotificationRepository;
    
    public AuctionWebSocketNotificationService(
            SimpMessagingTemplate messagingTemplate,
            AuctionRepository auctionRepository,
            LiveNotificationRepository liveNotificationRepository) {
        this.messagingTemplate = messagingTemplate;
        this.auctionRepository = auctionRepository;
        this.liveNotificationRepository = liveNotificationRepository;
    }

    private void persistLiveNotification(String auctionId, String content) {
        if (content == null || content.isBlank()) {
            return;
        }

        auctionRepository.findById(auctionId).ifPresent(auction -> {
            liveNotificationRepository.save(new LiveNotification(auction, content));
        });
    }
    
    /**
     * Send a bid update notification to all subscribers of the auction
     * @param auctionId The auction ID
     * @param bid The bid response
     */
    public void sendBidUpdate(String auctionId, BidResponse bid) {
        try {
            messagingTemplate.convertAndSend(
                "/topic/auction/" + auctionId + "/bids",
                bid
            );
            logger.debug("Sent bid update for auction {}", auctionId);
        } catch (Exception e) {
            logger.error("Error sending bid update for auction {}: {}", auctionId, e.getMessage());
        }
    }
    
    /**
     * Send a notification that the auction has been extended
     * @param auctionId The auction ID
     * @param newEndTime The new end time
     */
    public void sendAuctionExtensionNotification(String auctionId, OffsetDateTime newEndTime) {
        try {
            String message = "The auction has been extended by 5 minutes";
            persistLiveNotification(auctionId, message);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "AUCTION_EXTENDED");
            notification.put("auctionId", auctionId);
            notification.put("newEndTime", newEndTime);
            notification.put("message", message);
            
            messagingTemplate.convertAndSend(
                (Object) ("/topic/auction/" + auctionId + "/status"),
                notification
            );
            logger.info("Auction extension notification sent for auction {}, new end time: {}", auctionId, newEndTime);
        } catch (Exception e) {
            logger.error("Error sending auction extension notification for auction {}: {}", auctionId, e.getMessage());
        }
    }
    
    /**
     * Send a notification that the auction status has changed
     * @param auctionId The auction ID
     * @param status The new status
     */
    public void sendStatusChangeNotification(String auctionId, String status) {
        try {
            String message = "Auction status changed to " + status;
            persistLiveNotification(auctionId, message);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "STATUS_CHANGE");
            notification.put("auctionId", auctionId);
            notification.put("status", status);
            notification.put("timestamp", OffsetDateTime.now(ZoneOffset.ofHours(7)));
            notification.put("message", message);
            
            messagingTemplate.convertAndSend(
                (Object) ("/topic/auction/" + auctionId + "/status"),
                notification
            );
            logger.debug("Status change notification sent for auction {}: {}", auctionId, status);
        } catch (Exception e) {
            logger.error("Error sending status change notification for auction {}: {}", auctionId, e.getMessage());
        }
    }
    
    /**
     * Send a warning notification when bid is placed in the final 5 minutes
     * @param auctionId The auction ID
     * @param minutesRemaining Minutes remaining in the auction
     */
    public void sendFinalMinutesWarning(String auctionId, long minutesRemaining) {
        try {
            String message = "The auction is ending soon! " + minutesRemaining + " minutes remaining.";
            persistLiveNotification(auctionId, message);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FINAL_MINUTES_WARNING");
            notification.put("auctionId", auctionId);
            notification.put("minutesRemaining", minutesRemaining);
            notification.put("message", message);
            
            messagingTemplate.convertAndSend(
                (Object) ("/topic/auction/" + auctionId + "/warning"),
                notification
            );
            logger.debug("Final minutes warning sent for auction {}", auctionId);
        } catch (Exception e) {
            logger.error("Error sending final minutes warning for auction {}: {}", auctionId, e.getMessage());
        }
    }
    
    /**
     * Send admin notification to all participants of an auction
     * @param auctionId The auction ID
     * @param type The notification type (e.g., AUCTION_PAUSED, AUCTION_RESUMED)
     * @param message The notification message
     */
    public void sendAdminNotification(String auctionId, String type, String message) {
        try {
            persistLiveNotification(auctionId, message);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", type);
            notification.put("auctionId", auctionId);
            notification.put("message", message);
            notification.put("timestamp", OffsetDateTime.now(ZoneOffset.ofHours(7)));

            messagingTemplate.convertAndSend(
                (String) ("/topic/auction/" + auctionId + "/admin"),
                (Object) notification
            );
            logger.info("Admin notification sent for auction {}: type={}, message={}", auctionId, type, message);
        } catch (Exception e) {
            logger.error("Error sending admin notification for auction {}: {}", auctionId, e.getMessage());
        }
    }

    /**
     * Send a notification about auction ending soon
     * @param auctionId The auction ID
     */
    public void sendAuctionEndingNotification(String auctionId) {
        try {
            String message = "The auction is ending soon!";
            persistLiveNotification(auctionId, message);

            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "AUCTION_ENDING");
            notification.put("auctionId", auctionId);
            notification.put("message", message);
            
            messagingTemplate.convertAndSend(
                (Object) ("/topic/auction/" + auctionId + "/status"),
                notification
            );
            logger.debug("Auction ending notification sent for auction {}", auctionId);
        } catch (Exception e) {
            logger.error("Error sending auction ending notification for auction {}: {}", auctionId, e.getMessage());
        }
    }
}
