package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import io.github.guennhatking.libra_auction.enums.account.EmailStatus;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final CustomerService customerService;

    public AdminController(CustomerService customerService) {
        this.customerService = customerService;
    }

    private boolean isAdminUser(String userId) {
        Optional<io.github.guennhatking.libra_auction.models.person.Customer> user = customerService.findById(userId);
        if (user.isEmpty()) return false;
        return user.get().getRole() != null && "ADMIN".equalsIgnoreCase(user.get().getRole().getName());
    }

    @GetMapping("/users")
    public ResponseEntity<ServerAPIResponse<PageResponse<AdminPendingUserResponse>>> getUsers(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String identityNumber,
            @RequestParam(required = false) EmailStatus emailStatus,
            @RequestParam(required = false) AccountStatus accountStatus) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        PageResponse<AdminPendingUserResponse> result = customerService.searchAdminUsers(
                page,
                pageSize,
                name,
                email,
                phone,
                identityNumber,
                emailStatus,
                accountStatus);
        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<ServerAPIResponse<PageResponse<AdminPendingUserResponse>>> getPendingUsers(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        PageResponse<AdminPendingUserResponse> result = customerService.searchPendingUsers(page, pageSize);
        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<ServerAPIResponse<AdminPendingUserResponse>> approveUser(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId) {
        return updateUserStatus(userDetails, userId, AccountStatus.ACTIVE);
    }

    @PostMapping("/users/{userId}/reject")
    public ResponseEntity<ServerAPIResponse<AdminPendingUserResponse>> rejectUser(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId) {
        return updateUserStatus(userDetails, userId, AccountStatus.LOCKED);
    }

    @PostMapping("/users/{userId}/lock")
    public ResponseEntity<ServerAPIResponse<AdminPendingUserResponse>> lockUser(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId) {
        return updateUserStatus(userDetails, userId, AccountStatus.LOCKED);
    }

    @PostMapping("/users/{userId}/unlock")
    public ResponseEntity<ServerAPIResponse<AdminPendingUserResponse>> unlockUser(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId) {
        return updateUserStatus(userDetails, userId, AccountStatus.ACTIVE);
    }

    private ResponseEntity<ServerAPIResponse<AdminPendingUserResponse>> updateUserStatus(
            JwtUserDetails userDetails,
            String userId,
            AccountStatus status) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        try {
            return ResponseEntity.ok(ServerAPIResponse.success(customerService.updateAdminUserStatus(userId, status)));
        } catch (IllegalArgumentException error) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(error.getMessage()));
        }
    }
}
