package io.github.guennhatking.libra_auction.controllers;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.repositories.person.CustomerRepository;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.DashboardStatsService;
import io.github.guennhatking.libra_auction.viewmodels.response.AdminDashboardStatsResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.SellerDashboardStatsResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardStatsService dashboardStatsService;
    private final CustomerRepository customerRepository;

    public DashboardController(DashboardStatsService dashboardStatsService, CustomerRepository customerRepository) {
        this.dashboardStatsService = dashboardStatsService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/seller")
    public ResponseEntity<ServerAPIResponse<SellerDashboardStatsResponse>> getSellerStats(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        SellerDashboardStatsResponse stats = dashboardStatsService.getSellerStats(userDetails.getUserId());
        return ResponseEntity.ok(ServerAPIResponse.success(stats));
    }

    @GetMapping("/admin")
    public ResponseEntity<ServerAPIResponse<AdminDashboardStatsResponse>> getAdminStats(
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        AdminDashboardStatsResponse stats = dashboardStatsService.getAdminStats();
        return ResponseEntity.ok(ServerAPIResponse.success(stats));
    }

    private boolean isAdminUser(String userId) {
        Optional<Customer> user = customerRepository.findById(userId);
        if (user.isEmpty()) {
            return false;
        }
        return user.get().getRole() != null && "ADMIN".equalsIgnoreCase(user.get().getRole().getName());
    }
}
