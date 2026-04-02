package io.github.guennhatking.libra_auction.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.TaiKhoanOAuth;
import java.util.Optional;

public interface TaiKhoanOAuthRepository extends JpaRepository<TaiKhoanOAuth, String> {
    Optional<TaiKhoanOAuth> findByProviderAndProviderId(String provider, String providerId);
}
