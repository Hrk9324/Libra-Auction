package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.services.ProductSearchService;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.CustomerResponse;
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
    private final ProductSearchService productSearchService;

    public AdminController(CustomerService customerService, ProductSearchService productSearchService) {
        this.customerService = customerService;
        this.productSearchService = productSearchService;
    }

    private boolean isAdminUser(String userId) {
        Optional<io.github.guennhatking.libra_auction.models.person.Customer> user = customerService.findById(userId);
        if (user.isEmpty()) return false;
        return user.get().getRoles() != null && user.get().getRoles().stream()
                .anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getName()));
    }

    @GetMapping("/users/pending")
    public ResponseEntity<ServerAPIResponse<PageResponse<CustomerResponse>>> getPendingUsers(
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

        PageResponse<CustomerResponse> result = customerService.searchPendingUsers(page, pageSize);
        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    @GetMapping("/products/pending")
    public ResponseEntity<ServerAPIResponse<PageResponse<ProductResponse>>> getPendingProducts(
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

        // There is no explicit approval status on products in the model; return all products
        // as a fallback so frontend won't receive HTML 404. Use ProductSearchService for paging.
        io.github.guennhatking.libra_auction.viewmodels.request.ProductSearchRequest req =
                new io.github.guennhatking.libra_auction.viewmodels.request.ProductSearchRequest(
                        null, null, null, page, pageSize, "tenTaiSan", "DESC");

        PageResponse<ProductResponse> result = productSearchService.searchProducts(req);
        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }
}
