package io.github.guennhatking.libra_auction.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.DanhMuc;
import io.github.guennhatking.libra_auction.models.TaiSan;

public interface TaiSanRepository extends JpaRepository<TaiSan, String> {
    List<TaiSan> findByDanhMuc(DanhMuc danhMuc);
}
