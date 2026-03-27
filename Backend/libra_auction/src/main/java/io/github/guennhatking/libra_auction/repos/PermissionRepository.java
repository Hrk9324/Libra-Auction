package io.github.guennhatking.libra_auction.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import io.github.guennhatking.libra_auction.models.Permission;
@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {}