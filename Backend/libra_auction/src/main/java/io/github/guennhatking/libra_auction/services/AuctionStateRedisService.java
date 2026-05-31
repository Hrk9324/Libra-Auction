package io.github.guennhatking.libra_auction.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.Set;

/**
 * Service to manage auction state transitions using Redis ZSET (Sorted Set)
 * Stores auction events (start/end) with timestamp as score for efficient
 * scheduling
 */
@Service
public class AuctionStateRedisService {

    private static final String AUCTION_START_KEY = "auction:start:queue";
    private static final String AUCTION_END_KEY = "auction:end:queue";
    private static final String AUCTION_END_TIME_KEY = "auction:end:time";
    private static final String AUCTION_PAUSED_KEY = "auction:paused";

    private final RedisTemplate<String, String> redisTemplate;

    public AuctionStateRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add an auction to the start queue
     * 
     * @param auctionId The auction ID
     * @param startTime The start time
     */
    public void addAuctionStartEvent(String auctionId, OffsetDateTime startTime) {
        long timestamp = convertToMillis(startTime);
        redisTemplate.opsForZSet().add(AUCTION_START_KEY, auctionId, timestamp);
    }

    /**
     * Add an auction to the end queue
     * 
     * @param auctionId The auction ID
     * @param endTime   The end time
     */
    public void addAuctionEndEvent(String auctionId, OffsetDateTime endTime) {
        long timestamp = convertToMillis(endTime);
        redisTemplate.opsForZSet().add(AUCTION_END_KEY, auctionId, timestamp);
        // Also store the end time directly for quick access
        redisTemplate.opsForHash().put(AUCTION_END_TIME_KEY, auctionId, String.valueOf(timestamp));
    }

    /**
     * Get all auctions that should start before the given time
     * 
     * @param beforeTime The time threshold
     * @return Set of auction IDs that should start
     */
    public Set<String> getAuctionsToStart(OffsetDateTime beforeTime) {
        long timestamp = convertToMillis(beforeTime);
        return redisTemplate.opsForZSet().rangeByScore(AUCTION_START_KEY, 0, timestamp);
    }

    /**
     * Get all auctions that should end before the given time
     * 
     * @param beforeTime The time threshold
     * @return Set of auction IDs that should end
     */
    public Set<String> getAuctionsToEnd(OffsetDateTime beforeTime) {
        long timestamp = convertToMillis(beforeTime);
        return redisTemplate.opsForZSet().rangeByScore(AUCTION_END_KEY, 0, timestamp);
    }

    /**
     * Remove an auction from the start queue (after it has started)
     * 
     * @param auctionId The auction ID
     */
    public void removeAuctionStartEvent(String auctionId) {
        redisTemplate.opsForZSet().remove(AUCTION_START_KEY, auctionId);
    }

    /**
     * Remove an auction from the end queue (after it has ended)
     * 
     * @param auctionId The auction ID
     */
    public void removeAuctionEndEvent(String auctionId) {
        redisTemplate.opsForZSet().remove(AUCTION_END_KEY, auctionId);
        redisTemplate.opsForHash().delete(AUCTION_END_TIME_KEY, auctionId);
    }

    /**
     * Extend an auction end time by adding more minutes
     * If the auction is in the last 5 minutes and receives a bid, extend by 5
     * minutes
     * 
     * @param auctionId  The auction ID
     * @param newEndTime The new end time
     */
    public void extendAuctionEnd(String auctionId, OffsetDateTime newEndTime) {
        long newTimestamp = convertToMillis(newEndTime);
        // Update in the ZSET queue
        redisTemplate.opsForZSet().add(AUCTION_END_KEY, auctionId, newTimestamp);
        // Update the end time hash
        redisTemplate.opsForHash().put(AUCTION_END_TIME_KEY, auctionId, String.valueOf(newTimestamp));
    }

    /**
     * Get the current end time for an auction
     * 
     * @param auctionId The auction ID
     * @return The end time in milliseconds, or null if not found
     */
    public Long getAuctionEndTime(String auctionId) {
        Object endTimeObj = redisTemplate.opsForHash().get(AUCTION_END_TIME_KEY, auctionId);
        if (endTimeObj != null) {
            return Long.parseLong(endTimeObj.toString());
        }
        return null;
    }

    /**
     * Check if an auction is within the last N minutes of ending
     * 
     * @param auctionId     The auction ID
     * @param minutesWindow The window in minutes
     * @return true if within the window, false otherwise
     */
    public boolean isWithinFinalMinutes(String auctionId, int minutesWindow) {
        Long endTime = getAuctionEndTime(auctionId);
        if (endTime == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        long windowMillis = minutesWindow * 60 * 1000L;

        return now >= endTime - windowMillis && now < endTime;
    }

    /**
     * Mark an auction as paused and record the pause start time
     *
     * @param auctionId The auction ID
     * @param pauseTime The time when pause started
     */
    public void setPaused(String auctionId, OffsetDateTime pauseTime) {
        long timestamp = convertToMillis(pauseTime);
        redisTemplate.opsForHash().put(AUCTION_PAUSED_KEY, auctionId, String.valueOf(timestamp));
    }

    /**
     * Check if an auction is currently paused
     *
     * @param auctionId The auction ID
     * @return true if paused, false otherwise
     */
    public boolean isPaused(String auctionId) {
        return redisTemplate.opsForHash().hasKey(AUCTION_PAUSED_KEY, auctionId);
    }

    /**
     * Get the pause start time for an auction
     *
     * @param auctionId The auction ID
     * @return The pause start time in milliseconds, or null if not paused
     */
    public Long getPauseStartTime(String auctionId) {
        Object val = redisTemplate.opsForHash().get(AUCTION_PAUSED_KEY, auctionId);
        if (val != null) {
            return Long.parseLong(val.toString());
        }
        return null;
    }

    /**
     * Clear the paused state for an auction
     *
     * @param auctionId The auction ID
     */
    public void clearPaused(String auctionId) {
        redisTemplate.opsForHash().delete(AUCTION_PAUSED_KEY, auctionId);
    }

    /**
     * Clean up an auction from all Redis tracking structures
     *
     * @param auctionId The auction ID
     */
    public void cleanupAuction(String auctionId) {
        removeAuctionStartEvent(auctionId);
        removeAuctionEndEvent(auctionId);
        clearPaused(auctionId);
    }

    /**
     * Convert OffsetDateTime to milliseconds since epoch
     */
    private long convertToMillis(OffsetDateTime dateTime) {
        return dateTime.toInstant().toEpochMilli();
    }
}
