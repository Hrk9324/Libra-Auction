package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @Size(min = 3, message = "USERNAME_INVALID")
    String username,

    @Size(min = 6, message = "INVALID_PASSWORD")
    String password,

    @Email(message = "INVALID_EMAIL")
    String email,

    String fullName
) {
}
