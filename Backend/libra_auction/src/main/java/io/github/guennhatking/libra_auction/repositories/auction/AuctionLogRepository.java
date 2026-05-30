package io.github.guennhatking.libra_auction.repositories.auction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.guennhatking.libra_auction.models.auction.AuctionLog;

public interface AuctionLogRepository extends JpaRepository<AuctionLog, String> {

    @Query("SELECT al FROM AuctionLog al JOIN FETCH al.bidder WHERE al.auction.id = :auctionId ORDER BY al.timestamp DESC")
    List<AuctionLog> findByAuctionId(@Param("auctionId") String auctionId);
}
