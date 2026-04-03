package io.github.guennhatking.libra_auction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.ThongBao;

public interface ThongBaoRepository extends JpaRepository<ThongBao, String> {
}
