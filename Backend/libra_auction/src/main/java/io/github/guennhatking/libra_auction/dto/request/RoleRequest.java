package io.github.guennhatking.libra_auction.dto.request;

import java.util.Set;

import io.github.guennhatking.libra_auction.models.Permission;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleRequest {
    String name;
    String description;
    Set<Permission> permissions;
}