package io.github.guennhatking.libra_auction.viewmodels.response;

import java.time.LocalDateTime;

public class BidResponse {
    private String auctionId;
    private Long bidAmount;
    private String bidderId;
    private String bidderName;
    private LocalDateTime bidTime;
    private String status;

    public BidResponse() {
    }

    public BidResponse(String auctionId, Long bidAmount, String bidderId, String bidderName, LocalDateTime bidTime, String status) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
        this.bidTime = bidTime;
        this.status = status;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public Long getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(Long bidAmount) {
        this.bidAmount = bidAmount;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public String getBidderName() {
        return bidderName;
    }

    public void setBidderName(String bidderName) {
        this.bidderName = bidderName;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public void setBidTime(LocalDateTime bidTime) {
        this.bidTime = bidTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
