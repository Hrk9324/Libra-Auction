package io.github.guennhatking.libra_auction.repositories.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import io.github.guennhatking.libra_auction.models.request.OtpRequest;
import java.util.Optional;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, String> {
    @Query("SELECT o FROM OtpRequest o WHERE o.nguoiDung.email = ?1 ORDER BY o.id DESC LIMIT 1")
    Optional<OtpRequest> findLatestByEmail(String email);
}
