package io.github.guennhatking.libra_auction.viewmodels.response;

import java.util.Set;
import io.github.guennhatking.libra_auction.models.Permission;

public record RoleResponse(
    String name,
    String description,
    Set<Permission> permissions
) {
}
