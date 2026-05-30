package io.github.guennhatking.libra_auction.repositories.transaction;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.guennhatking.libra_auction.models.transaction.DepositTransaction;
import io.github.guennhatking.libra_auction.enums.transaction.TransactionStatus;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, String> {

    @Query("SELECT d FROM DepositTransaction d WHERE d.depositor.id = :userId AND d.participationInfo.auction.id = :auctionId AND d.transactionStatus = :status")
    Optional<DepositTransaction> findByDepositorIdAndAuctionIdAndStatus(
            @Param("userId") String userId,
            @Param("auctionId") String auctionId,
            @Param("status") TransactionStatus status);
}
