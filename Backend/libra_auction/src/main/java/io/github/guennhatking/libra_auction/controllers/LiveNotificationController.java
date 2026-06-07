package io.github.guennhatking.libra_auction.controllers;

import java.util.List;

import io.github.guennhatking.libra_auction.models.notification.LiveNotification;
import io.github.guennhatking.libra_auction.repositories.notification.LiveNotificationRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.LiveNotificationResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/auctions")
public class LiveNotificationController {
    private final LiveNotificationRepository liveNotificationRepository;

    public LiveNotificationController(LiveNotificationRepository liveNotificationRepository) {
        this.liveNotificationRepository = liveNotificationRepository;
    }

    @GetMapping("/{auctionId}/live-notifications")
    public ResponseEntity<ServerAPIResponse<List<LiveNotificationResponse>>> getLiveNotifications(
            @PathVariable String auctionId) {
        List<LiveNotificationResponse> notifications = liveNotificationRepository
                .findByAuction_IdOrderBySentAtDesc(auctionId)
                .stream()
                .map(LiveNotificationController::toResponse)
                .toList();
        return ResponseEntity.ok(ServerAPIResponse.success(notifications));
    }

    private static LiveNotificationResponse toResponse(LiveNotification notification) {
        return new LiveNotificationResponse(
                notification.getId(),
                notification.getAuction().getId(),
                notification.getContent(),
                notification.getSentAt());
    }
}