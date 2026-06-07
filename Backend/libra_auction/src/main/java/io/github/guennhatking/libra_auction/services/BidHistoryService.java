package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.auction.AuctionLog;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionLogRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BidHistoryService {

    private final AuctionLogRepository auctionLogRepository;

    public BidHistoryService(AuctionLogRepository auctionLogRepository) {
        this.auctionLogRepository = auctionLogRepository;
    }

    @Transactional(readOnly = true)
    public List<BidResponse> getAuctionBids(String auctionId) {
        return auctionLogRepository.findByAuctionId(auctionId).stream()
            .map(log -> toBidResponse(auctionId, log))
            .toList();
    }

    @Transactional(readOnly = true)
    public int getAuctionBidsCount(String auctionId) {
        return auctionLogRepository.countByAuctionId(auctionId);
    }

    @Transactional(readOnly = true)
    public BidResponse getLatestBid(String auctionId) {
        return auctionLogRepository.findFirstByAuction_IdOrderByTimestampDesc(auctionId)
            .map(log -> toBidResponse(auctionId, log))
            .orElse(null);
    }

    private BidResponse toBidResponse(String auctionId, AuctionLog log) {
        return new BidResponse(
            auctionId,
            log.getBidAmount(),
            log.getBidder() != null ? log.getBidder().getId() : null,
            log.getBidder() != null ? log.getBidder().getFullName() : "Unknown",
            log.getTimestamp(),
            "SUCCESS"
        );
    }
}
