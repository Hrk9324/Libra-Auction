package io.github.guennhatking.libra_auction.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.PhienDauGia;
import io.github.guennhatking.libra_auction.models.TaiSan;

public interface PhienDauGiaRepository extends JpaRepository<PhienDauGia, String> {
    List<PhienDauGia> findByTrangThaiPhien(Enums.TrangThaiPhien trangThaiPhien);
    List<PhienDauGia> findByLoaiDauGia(Enums.LoaiDauGia loaiDauGia);
    List<PhienDauGia> findByTaiSan(TaiSan taiSan);
}
