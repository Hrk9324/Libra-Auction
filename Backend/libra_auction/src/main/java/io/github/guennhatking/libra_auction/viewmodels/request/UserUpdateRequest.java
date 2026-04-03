package io.github.guennhatking.libra_auction.viewmodels.request;

import java.time.LocalDate;
import java.util.Set;
import io.github.guennhatking.libra_auction.models.Role;
import io.github.guennhatking.libra_auction.validators.DobConstraint;

public record UserUpdateRequest(
    String password,
    String fullName,

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob,

    Set<Role> roles
) {
}
