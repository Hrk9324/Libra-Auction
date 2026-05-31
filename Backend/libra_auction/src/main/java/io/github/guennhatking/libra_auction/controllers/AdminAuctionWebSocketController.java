package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.models.auction.Auction;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionRepository;
import io.github.guennhatking.libra_auction.services.AuctionStateTransitionService;
import io.github.guennhatking.libra_auction.services.AuctionWebSocketNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Optional;

/**
 * WebSocket Controller for admin auction control commands
 */
@Controller
public class AdminAuctionWebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuctionWebSocketController.class);

    private final AuctionRepository auctionRepository;
    private final AuctionStateTransitionService auctionStateTransitionService;
    private final AuctionWebSocketNotificationService notificationService;

    public AdminAuctionWebSocketController(
            AuctionRepository auctionRepository,
            AuctionStateTransitionService auctionStateTransitionService,
            AuctionWebSocketNotificationService notificationService) {
        this.auctionRepository = auctionRepository;
        this.auctionStateTransitionService = auctionStateTransitionService;
        this.notificationService = notificationService;
    }

    /**
     * Pause an auction
     */
    @MessageMapping("/admin/auction/{auctionId}/pause")
    public void pauseAuction(@DestinationVariable String auctionId) {
        logger.info("Admin command: PAUSE auction {}", auctionId);

        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            logger.warn("Auction not found: {}", auctionId);
            return;
        }

        Auction auction = auctionOpt.get();
        if (auction.getAuctionStatus() != AuctionStatus.IN_PROGRESS) {
            logger.warn("Cannot pause auction {} - current status: {}", auctionId, auction.getAuctionStatus());
            notificationService.sendAdminNotification(auctionId, "ERROR",
                    "Không thể tạm dừng phiên đấu giá. Trạng thái hiện tại: " + auction.getAuctionStatus());
            return;
        }

        auctionStateTransitionService.pauseAuction(auctionId);

        notificationService.sendAdminNotification(auctionId, "AUCTION_PAUSED",
                "Phiên đấu giá đã được tạm dừng bởi quản trị viên");
    }

    /**
     * Resume a paused auction
     */
    @MessageMapping("/admin/auction/{auctionId}/resume")
    public void resumeAuction(@DestinationVariable String auctionId) {
        logger.info("Admin command: RESUME auction {}", auctionId);

        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            logger.warn("Auction not found: {}", auctionId);
            return;
        }

        Auction auction = auctionOpt.get();
        if (auction.getAuctionStatus() != AuctionStatus.PAUSED) {
            logger.warn("Cannot resume auction {} - current status: {}", auctionId, auction.getAuctionStatus());
            notificationService.sendAdminNotification(auctionId, "ERROR",
                    "Không thể tiếp tục phiên đấu giá. Trạng thái hiện tại: " + auction.getAuctionStatus());
            return;
        }

        auctionStateTransitionService.resumeAuction(auctionId);

        notificationService.sendAdminNotification(auctionId, "AUCTION_RESUMED",
                "Phiên đấu giá đã được tiếp tục bởi quản trị viên");
    }

    /**
     * End an auction
     */
    @MessageMapping("/admin/auction/{auctionId}/end")
    public void endAuction(@DestinationVariable String auctionId) {
        logger.info("Admin command: END auction {}", auctionId);

        Optional<Auction> auctionOpt = auctionRepository.findById(auctionId);
        if (auctionOpt.isEmpty()) {
            logger.warn("Auction not found: {}", auctionId);
            return;
        }

        Auction auction = auctionOpt.get();
        if (auction.getAuctionStatus() != AuctionStatus.IN_PROGRESS
                && auction.getAuctionStatus() != AuctionStatus.PAUSED) {
            logger.warn("Cannot end auction {} - current status: {}", auctionId, auction.getAuctionStatus());
            notificationService.sendAdminNotification(auctionId, "ERROR",
                    "Không thể kết thúc phiên đấu giá. Trạng thái hiện tại: " + auction.getAuctionStatus());
            return;
        }

        auctionStateTransitionService.endAuction(auctionId);

        notificationService.sendAdminNotification(auctionId, "AUCTION_ENDED",
                "Phiên đấu giá đã được kết thúc bởi quản trị viên");
    }

    /**
     * Cancel an auction (only NOT_STARTED can be cancelled, reason required)
     */
    @MessageMapping("/admin/auction/{auctionId}/cancel")
    public void cancelAuction(@DestinationVariable String auctionId, @Payload Map<String, String> payload) {
        String reason = payload != null ? payload.getOrDefault("reason", "") : "";
        logger.info("Admin command: CANCEL auction {}, reason: {}", auctionId, reason);

        try {
            auctionStateTransitionService.cancelAuction(auctionId, reason);
            notificationService.sendAdminNotification(auctionId, "AUCTION_CANCELLED",
                    "Phiên đấu giá đã bị hủy. Lý do: " + (reason.isEmpty() ? "Không có" : reason));
        } catch (IllegalStateException e) {
            logger.warn("Cannot cancel auction {}: {}", auctionId, e.getMessage());
            notificationService.sendAdminNotification(auctionId, "ERROR", e.getMessage());
        }
    }

    /**
     * Send notification message to all participants
     */
    @MessageMapping("/admin/auction/{auctionId}/notify")
    public void sendNotification(@DestinationVariable String auctionId, @Payload Map<String, String> payload) {
        String message = payload.getOrDefault("message", "");
        logger.info("Admin notification for auction {}: {}", auctionId, message);

        notificationService.sendAdminNotification(auctionId, "ADMIN_NOTIFICATION", message);
    }
}
