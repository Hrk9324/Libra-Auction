package io.github.guennhatking.libra_auction.dto.response;

import java.util.Set;

import io.github.guennhatking.libra_auction.models.Permission;

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
public class RoleResponse {
    String name;
    String description;
    Set<Permission> permissions;
}

