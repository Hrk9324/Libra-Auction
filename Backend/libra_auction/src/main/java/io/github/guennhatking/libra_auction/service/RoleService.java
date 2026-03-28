package io.github.guennhatking.libra_auction.service;

import java.util.HashSet;
import java.util.List;

import org.springframework.stereotype.Service;

import io.github.guennhatking.libra_auction.dto.request.RoleRequest;
import io.github.guennhatking.libra_auction.dto.response.RoleResponse;
import io.github.guennhatking.libra_auction.mapper.RoleMapper;
import io.github.guennhatking.libra_auction.repos.PermissionRepository;
import io.github.guennhatking.libra_auction.repos.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            role.setPermissions(new HashSet<>(request.getPermissions()));
        }

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}