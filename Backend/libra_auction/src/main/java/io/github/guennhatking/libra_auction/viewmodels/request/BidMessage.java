package io.github.guennhatking.libra_auction.viewmodels.request;

public class BidMessage {
    private String auctionId;
    private Long bidAmount;
    private String bidderId;
    private String bidderName;

    public BidMessage() {
    }

    public BidMessage(String auctionId, Long bidAmount, String bidderId, String bidderName) {
        this.auctionId = auctionId;
        this.bidAmount = bidAmount;
        this.bidderId = bidderId;
        this.bidderName = bidderName;
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
}
