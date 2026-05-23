package io.github.guennhatking.libra_auction.repositories.auction;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.guennhatking.libra_auction.enums.auction.TrangThaiPhien;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.product.TaiSan;

public interface PhienDauGiaRepository extends JpaRepository<PhienDauGia, String> {
    Optional<PhienDauGia> findById(String id);

    List<PhienDauGia> findByTrangThaiPhien(TrangThaiPhien trangThaiPhien);

    List<PhienDauGia> findByTaiSan(TaiSan taiSan);

    Optional<PhienDauGia> findByIdAndTaiSan_DanhMuc_Id(String id, String categoryId);
}
