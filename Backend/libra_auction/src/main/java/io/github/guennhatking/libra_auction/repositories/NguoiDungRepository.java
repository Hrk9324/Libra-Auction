package io.github.guennhatking.libra_auction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import java.util.Optional;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
    Optional<NguoiDung> findByEmail(String email);
}
