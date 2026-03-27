package io.github.guennhatking.libra_auction.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import io.github.guennhatking.libra_auction.models.NguoiDung;
@Repository
public interface NguoiDungRepository extends JpaRepository<NguoiDung, String> {
    NguoiDung findByEmail(String email);

    NguoiDung findByHoVaTen(String hoVaTen);
}
