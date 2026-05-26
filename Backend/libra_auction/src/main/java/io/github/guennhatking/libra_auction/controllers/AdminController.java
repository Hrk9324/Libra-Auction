package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AdminPendingUserResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
        return user.get().getRoles() != null && user.get().getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
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

}
