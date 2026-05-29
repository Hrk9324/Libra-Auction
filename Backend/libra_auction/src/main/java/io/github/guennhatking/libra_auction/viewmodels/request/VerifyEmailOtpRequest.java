package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailOtpRequest(
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "INVALID_EMAIL")
    String email,

    @NotBlank(message = "OTP không được để trống.")
    String otp
) {}
