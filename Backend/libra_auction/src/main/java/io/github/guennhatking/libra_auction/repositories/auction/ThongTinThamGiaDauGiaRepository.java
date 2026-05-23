package io.github.guennhatking.libra_auction.repositories.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.auction.ThongTinThamGiaDauGia;

public interface ThongTinThamGiaDauGiaRepository extends JpaRepository<ThongTinThamGiaDauGia, String> {
    List<ThongTinThamGiaDauGia> findByNguoiThamGiaId(String userId);
    List<ThongTinThamGiaDauGia> findByPhienDauGiaId(String auctionId);
    Optional<ThongTinThamGiaDauGia> findByNguoiThamGiaIdAndPhienDauGiaId(String userId, String auctionId);
}
