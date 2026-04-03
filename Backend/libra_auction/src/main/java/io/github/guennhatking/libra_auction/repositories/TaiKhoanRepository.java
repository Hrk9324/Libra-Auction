package io.github.guennhatking.libra_auction.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.guennhatking.libra_auction.models.TaiKhoan;

public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, String> {
    TaiKhoan findByUsername(String username);
}
