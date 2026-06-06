package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.auction.AuctionLog;
import io.github.guennhatking.libra_auction.repositories.auction.AuctionLogRepository;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BidHistoryService {

    private static final Map<String, List<BidResponse>> auctionBids = new ConcurrentHashMap<>();
    private final AuctionLogRepository auctionLogRepository;

    public BidHistoryService(AuctionLogRepository auctionLogRepository) {
        this.auctionLogRepository = auctionLogRepository;
    }

    public void recordBid(BidResponse bid) {
        auctionBids.computeIfAbsent(bid.auctionId(), k -> new ArrayList<>())
            .add(bid);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> getAuctionBids(String auctionId) {
        List<BidResponse> inMemory = auctionBids.getOrDefault(auctionId, new ArrayList<>());
        if (!inMemory.isEmpty()) {
            return new ArrayList<>(inMemory);
        }

        // Fallback to DB
        List<AuctionLog> logs = auctionLogRepository.findByAuctionId(auctionId);
        List<BidResponse> result = new ArrayList<>();
        for (AuctionLog log : logs) {
            result.add(new BidResponse(
                auctionId,
                log.getBidAmount(),
                log.getBidder() != null ? log.getBidder().getId() : null,
                log.getBidder() != null ? log.getBidder().getFullName() : "Unknown",
                log.getTimestamp(),
                "SUCCESS"
            ));
        }
        return result;
    }

    public int getAuctionBidsCount(String auctionId) {
        return getAuctionBids(auctionId).size();
    }

    public BidResponse getLatestBid(String auctionId) {
        List<BidResponse> bids = getAuctionBids(auctionId);
        if (!bids.isEmpty()) {
            return bids.get(bids.size() - 1);
        }
        return null;
    }
}
