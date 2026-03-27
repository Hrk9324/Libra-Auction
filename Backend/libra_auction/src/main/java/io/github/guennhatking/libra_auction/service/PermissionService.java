package io.github.guennhatking.libra_auction.service;

import java.util.List;

import org.springframework.stereotype.Service;

import io.github.guennhatking.libra_auction.dto.request.PermissionRequest;
import io.github.guennhatking.libra_auction.dto.response.PermissionResponse;
import io.github.guennhatking.libra_auction.mapper.PermissionMapper;
import io.github.guennhatking.libra_auction.repos.PermissionRepository;
import io.github.guennhatking.libra_auction.models.Permission;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}