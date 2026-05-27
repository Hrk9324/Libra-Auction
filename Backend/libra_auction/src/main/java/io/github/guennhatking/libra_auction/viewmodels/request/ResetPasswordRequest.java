package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "INVALID_EMAIL")
    String email,

    @NotBlank(message = "Mật khẩu mới không được để trống.")
    @Size(min = 6, message = "INVALID_PASSWORD")
    String newPassword
) {}
