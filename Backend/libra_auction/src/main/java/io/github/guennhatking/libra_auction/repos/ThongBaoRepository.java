package io.github.guennhatking.libra_auction.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.ThongBao;

public interface ThongBaoRepository extends JpaRepository<ThongBao, String> {
}
