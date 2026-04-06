package io.github.guennhatking.libra_auction.repositories;

import io.github.guennhatking.libra_auction.models.HinhAnhTaiSan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HinhAnhTaiSanRepository extends JpaRepository<HinhAnhTaiSan, String> {
	List<HinhAnhTaiSan> findByTaiSanIdOrderByThuTuHienThiAsc(String taiSanId);
}
