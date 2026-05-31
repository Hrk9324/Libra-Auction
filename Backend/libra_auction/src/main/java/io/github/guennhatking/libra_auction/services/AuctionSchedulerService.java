package io.github.guennhatking.libra_auction.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Service
@EnableScheduling
public class AuctionSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuctionSchedulerService.class);
    
    private final AuctionStateRedisService auctionStateRedisService;
    private final AuctionStateTransitionService auctionStateTransitionService;
    
    public AuctionSchedulerService(
            AuctionStateRedisService auctionStateRedisService,
            AuctionStateTransitionService auctionStateTransitionService) {
        this.auctionStateRedisService = auctionStateRedisService;
        this.auctionStateTransitionService = auctionStateTransitionService;
    }
    
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void checkAuctionStarting() {
        try {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(7));
            Set<String> auctionsToStart = auctionStateRedisService.getAuctionsToStart(now);
            
            if (!auctionsToStart.isEmpty()) {
                logger.info("Found {} auctions to start", auctionsToStart.size());
                
                for (String auctionId : auctionsToStart) {
                    try {
                        auctionStateTransitionService.startAuction(auctionId);
                        auctionStateRedisService.removeAuctionStartEvent(auctionId);
                    } catch (Exception e) {
                        logger.error("Error starting auction {}: {}", auctionId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in checkAuctionStarting: {}", e.getMessage(), e);
        }
    }
    
    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    public void checkAuctionEnding() {
        try {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(7));
            Set<String> auctionsToEnd = auctionStateRedisService.getAuctionsToEnd(now);

            if (!auctionsToEnd.isEmpty()) {
                for (String auctionId : auctionsToEnd) {
                    try {
                        // Skip if auction is paused - endTime will be extended when resumed
                        if (auctionStateRedisService.isPaused(auctionId)) {
                            continue;
                        }
                        auctionStateTransitionService.endAuction(auctionId);
                        auctionStateRedisService.removeAuctionEndEvent(auctionId);
                    } catch (Exception e) {
                        logger.error("Error ending auction {}: {}", auctionId, e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error in checkAuctionEnding: {}", e.getMessage(), e);
        }
    }
}
