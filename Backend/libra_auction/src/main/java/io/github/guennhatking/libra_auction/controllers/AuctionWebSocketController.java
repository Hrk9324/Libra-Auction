package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.repositories.PhienDauGiaRepository;
import io.github.guennhatking.libra_auction.viewmodels.request.BidMessage;
import io.github.guennhatking.libra_auction.viewmodels.response.BidResponse;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AuctionWebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final PhienDauGiaRepository phienDauGiaRepository;
    
    // In-memory storage for bid history per auction
    private static final Map<String, List<BidResponse>> auctionBids = new ConcurrentHashMap<>();

    public AuctionWebSocketController(SimpMessagingTemplate messagingTemplate,
                                      PhienDauGiaRepository phienDauGiaRepository) {
        this.messagingTemplate = messagingTemplate;
        this.phienDauGiaRepository = phienDauGiaRepository;
    }

    @MessageMapping("/bid")
    public void handleBid(BidMessage bidMessage) {
        try {
            // Validate auction exists
            PhienDauGia auction = phienDauGiaRepository.findById(bidMessage.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"));

            // Validate bid amount
            if (bidMessage.getBidAmount() < auction.getGiaKhoiDiem()) {
                sendErrorNotification(bidMessage.getAuctionId(), 
                    "Bid amount must be at least: " + auction.getGiaKhoiDiem());
                return;
            }

            // Create bid response
            BidResponse bidResponse = new BidResponse(
                bidMessage.getAuctionId(),
                bidMessage.getBidAmount(),
                bidMessage.getBidderId(),
                bidMessage.getBidderName(),
                LocalDateTime.now(),
                "SUCCESS"
            );

            // Store bid in memory
            auctionBids.computeIfAbsent(bidMessage.getAuctionId(), k -> new ArrayList<>())
                .add(bidResponse);

            // Update current price
            auction.setGiaHienTai(bidMessage.getBidAmount());
            phienDauGiaRepository.save(auction);

            // Broadcast to all users subscribed to this auction
            messagingTemplate.convertAndSend("/topic/auction/" + bidMessage.getAuctionId(), bidResponse);

        } catch (Exception e) {
            sendErrorNotification(bidMessage.getAuctionId(), e.getMessage());
        }
    }

    private void sendErrorNotification(String auctionId, String errorMessage) {
        BidResponse errorResponse = new BidResponse();
        errorResponse.setStatus("ERROR");
        errorResponse.setAuctionId(auctionId);
        BidResponse error = new BidResponse();
        error.setStatus("ERROR");
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId, error);
    }
}
