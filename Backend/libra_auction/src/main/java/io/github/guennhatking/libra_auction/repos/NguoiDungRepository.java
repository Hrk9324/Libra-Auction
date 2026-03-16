package io.github.guennhatking.libra_auction.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.NguoiDung;

public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
}
