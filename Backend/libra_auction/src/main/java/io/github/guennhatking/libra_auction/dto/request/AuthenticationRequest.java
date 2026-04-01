package io.github.guennhatking.libra_auction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationRequest {
    @NotBlank(message = "USERNAME_INVALID")
    String username;
    
    @NotBlank(message = "INVALID_PASSWORD")
    String password;
}

