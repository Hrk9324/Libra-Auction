package io.github.guennhatking.libra_auction.models.notification;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import io.github.guennhatking.libra_auction.models.auction.Auction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "live_notification")
public class LiveNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private OffsetDateTime sentAt;

    protected LiveNotification() {
    }

    public LiveNotification(Auction auction, String content) {
        this.auction = auction;
        this.content = content;
        this.sentAt = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    public String getId() {
        return id;
    }

    public Auction getAuction() {
        return auction;
    }

    public String getContent() {
        return content;
    }

    public OffsetDateTime getSentAt() {
        return sentAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSentAt(OffsetDateTime sentAt) {
        this.sentAt = sentAt;
    }
}