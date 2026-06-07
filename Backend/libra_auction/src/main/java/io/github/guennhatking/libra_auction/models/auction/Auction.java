package io.github.guennhatking.libra_auction.models.auction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import io.github.guennhatking.libra_auction.enums.auction.ApprovalStatus;
import io.github.guennhatking.libra_auction.enums.auction.AuctionStatus;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.models.product.Product;
import io.github.guennhatking.libra_auction.models.qa.Question;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private Customer creator;

    private OffsetDateTime startTime;
    private OffsetDateTime endTime;
    private long duration;

    private long depositAmount;
    private long startingPrice;
    private long minimumBidIncrement;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "auction")
    private AuctionResult auctionResult;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auction")
    private List<Question> questions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auction")
    private List<AuctionLog> bidHistory;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auction")
    private List<AuctionParticipationInfo> participants;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "auction")
    private List<BidRejectionRecord> rejectionRecords;

    @Enumerated(EnumType.STRING)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    private AuctionStatus auctionStatus;

    private OffsetDateTime createdAt;
    private OffsetDateTime completedAt;
    private String failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    // CONSTRUCTOR
    public Auction() {
    }

    public Auction(
            Customer creator,
            Product product,
            OffsetDateTime startTime,
            long duration,
            long depositAmount,
            long startingPrice,
            long minimumBidIncrement) {
        this.creator = creator;
        this.product = product;
        this.startTime = startTime;
        this.duration = duration;
        this.depositAmount = depositAmount;
        this.startingPrice = startingPrice;
        this.minimumBidIncrement = minimumBidIncrement;
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Auction(
            Customer creator,
            Product product,
            OffsetDateTime startTime,
            long duration,
            long depositAmount,
            long startingPrice,
            long minimumBidIncrement,
            ApprovalStatus approvalStatus,
            AuctionStatus auctionStatus,
            OffsetDateTime createdAt) {
        this.creator = creator;
        this.product = product;
        this.startTime = startTime;
        this.duration = duration;
        this.depositAmount = depositAmount;
        this.startingPrice = startingPrice;
        this.minimumBidIncrement = minimumBidIncrement;
        this.approvalStatus = approvalStatus;
        this.auctionStatus = auctionStatus;
        this.createdAt = createdAt != null ? createdAt : OffsetDateTime.now(ZoneOffset.UTC);
    }

    // GETTER
    public String getId() {
        return id;
    }

    public Customer getCreator() {
        return creator;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public long getDuration() {
        return duration;
    }

    public AuctionResult getAuctionResult() {
        return auctionResult;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<AuctionLog> getBidHistory() {
        return bidHistory;
    }

    public List<AuctionParticipationInfo> getParticipants() {
        return participants;
    }

    public List<BidRejectionRecord> getRejectionRecords() {
        return rejectionRecords;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public AuctionStatus getAuctionStatus() {
        return auctionStatus;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Product getProduct() {
        return product;
    }

    public long getStartingPrice() {
        return startingPrice;
    }

    public long getDepositAmount() {
        return depositAmount;
    }

    public long getMinimumBidIncrement() {
        return minimumBidIncrement;
    }

    /**
     * Compute current price from bid history.
     * Returns 0 if there are no bids; callers may fall back to startingPrice.
     */
    public long getCurrentPrice() {
        if (bidHistory == null || bidHistory.isEmpty()) {
            return 0L;
        }
        return bidHistory.stream()
                .mapToLong(AuctionLog::getBidAmount)
                .max()
                .orElse(0L);
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setCreator(Customer creator) {
        this.creator = creator;
    }

    public void setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(OffsetDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setAuctionResult(AuctionResult auctionResult) {
        this.auctionResult = auctionResult;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void setBidHistory(List<AuctionLog> bidHistory) {
        this.bidHistory = bidHistory;
    }

    public void setParticipants(List<AuctionParticipationInfo> participants) {
        this.participants = participants;
    }

    public void setRejectionRecords(List<BidRejectionRecord> rejectionRecords) {
        this.rejectionRecords = rejectionRecords;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public void setAuctionStatus(AuctionStatus auctionStatus) {
        this.auctionStatus = auctionStatus;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setStartingPrice(long startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setDepositAmount(long depositAmount) {
        this.depositAmount = depositAmount;
    }

    public void setMinimumBidIncrement(long minimumBidIncrement) {
        this.minimumBidIncrement = minimumBidIncrement;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
