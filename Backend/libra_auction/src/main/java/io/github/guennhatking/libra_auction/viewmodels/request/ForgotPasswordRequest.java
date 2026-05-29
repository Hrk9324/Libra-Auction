package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "INVALID_EMAIL")
    String email
) {}
