package io.github.guennhatking.libra_auction.viewmodels.response;

import java.util.Set;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.Role;

public record UserResponse(
    String id,
    String hoVaTen,
    String soDienThoai,
    String CCCD,
    String email,
    String anhDaiDien,
    Enums.TrangThaiEmail trangThaiEmail,
    Enums.TrangThaiTaiKhoan trangThaiTaiKhoan,
    Set<Role> roles
) {
}
