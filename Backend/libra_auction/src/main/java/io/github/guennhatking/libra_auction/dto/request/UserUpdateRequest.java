package io.github.guennhatking.libra_auction.dto.request;

import java.time.LocalDate;
import java.util.Set;

import io.github.guennhatking.libra_auction.models.Role;
import io.github.guennhatking.libra_auction.validator.DobConstraint;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String fullName;

    @DobConstraint(min = 18, message = "INVALID_DOB")
    LocalDate dob;

    Set<Role> roles;
}