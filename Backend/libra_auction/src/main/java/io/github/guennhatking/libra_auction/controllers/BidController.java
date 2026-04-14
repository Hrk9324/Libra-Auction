package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.BidHistoryService;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class BidController {

    private final BidHistoryService bidHistoryService;

    public BidController(BidHistoryService bidHistoryService) {
        this.bidHistoryService = bidHistoryService;
    }

    @GetMapping("/{auctionId}/bids")
    public ResponseEntity<List<BidResponse>> getAuctionBids(@PathVariable String auctionId) {
        return ResponseEntity.ok(bidHistoryService.getAuctionBids(auctionId));
    }

    @GetMapping("/{auctionId}/bids/count")
    public ResponseEntity<Integer> getAuctionBidsCount(@PathVariable String auctionId) {
        return ResponseEntity.ok(bidHistoryService.getAuctionBidsCount(auctionId));
    }
}
