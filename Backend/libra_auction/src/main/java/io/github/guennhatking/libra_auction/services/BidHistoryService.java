package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BidHistoryService {
    
    private static final Map<String, List<BidResponse>> auctionBids = new ConcurrentHashMap<>();

    public void recordBid(BidResponse bid) {
        auctionBids.computeIfAbsent(bid.getAuctionId(), k -> new ArrayList<>())
            .add(bid);
    }

    public List<BidResponse> getAuctionBids(String auctionId) {
        return new ArrayList<>(auctionBids.getOrDefault(auctionId, new ArrayList<>()));
    }

    public int getAuctionBidsCount(String auctionId) {
        return auctionBids.getOrDefault(auctionId, new ArrayList<>()).size();
    }

    public BidResponse getLatestBid(String auctionId) {
        List<BidResponse> bids = auctionBids.get(auctionId);
        if (bids != null && !bids.isEmpty()) {
            return bids.get(bids.size() - 1);
        }
        return null;
    }
}
