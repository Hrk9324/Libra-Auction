package io.github.guennhatking.libra_auction.repositories.notification;

import java.util.List;

import io.github.guennhatking.libra_auction.models.notification.LiveNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiveNotificationRepository extends JpaRepository<LiveNotification, String> {
    List<LiveNotification> findByAuction_IdOrderBySentAtDesc(String auctionId);
}