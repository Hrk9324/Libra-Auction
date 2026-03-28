package io.github.guennhatking.libra_auction.dto.response;

import java.time.LocalDate;
import java.util.Set;

import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class UserResponse {
    String id;
    String hoVaTen;
    String soDienThoai;
    String CCCD;
    String email;
    String anhDaiDien;

    Enums.TrangThaiEmail trangThaiEmail;
    Enums.TrangThaiTaiKhoan trangThaiTaiKhoan;
    Set<Role> roles;
}