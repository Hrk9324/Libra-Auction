package io.github.guennhatking.libra_auction.repositories.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import io.github.guennhatking.libra_auction.models.account.TaiKhoanPassword;
import java.util.Optional;

public interface TaiKhoanPasswordRepository extends JpaRepository<TaiKhoanPassword, String> {
    @Query("SELECT t FROM TaiKhoanPassword t LEFT JOIN FETCH t.nguoiDung WHERE t.username = :username")
    Optional<TaiKhoanPassword> findByUsername(String username);

    @Query("SELECT t FROM TaiKhoanPassword t LEFT JOIN FETCH t.nguoiDung nd WHERE nd.id = :userId")
    Optional<TaiKhoanPassword> findByNguoiDungId(String userId);

    @Query("SELECT t FROM TaiKhoanPassword t LEFT JOIN FETCH t.nguoiDung nd WHERE nd.email = :email")
    Optional<TaiKhoanPassword> findByNguoiDungEmail(String email);
}
