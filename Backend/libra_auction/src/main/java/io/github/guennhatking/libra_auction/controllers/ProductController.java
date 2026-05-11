package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.ProductService;
import io.github.guennhatking.libra_auction.services.ProductSearchService;
import io.github.guennhatking.libra_auction.services.UserService;
import io.github.guennhatking.libra_auction.services.AuctionService;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductSearchRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ProductSearchRequestWrapper;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ProductResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import jakarta.validation.Valid;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;
    private final ProductSearchService productSearchService;
    private final UserService userService;
    private final AuctionService auctionService;

    public ProductController(ProductService productService, ProductSearchService productSearchService,
            UserService userService, AuctionService auctionService) {
        this.productService = productService;
        this.productSearchService = productSearchService;
        this.userService = userService;
        this.auctionService = auctionService;
    }

    // Helper method to check if user is admin
    private boolean isAdminUser(String userId) {
        Optional<NguoiDung> user = userService.findById(userId);
        if (user.isEmpty()) {
            return false;
        }
        return user.get().getRoles() != null && 
               user.get().getRoles().stream()
                   .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()));
    }

    @GetMapping("/public/products")
    public ResponseEntity<ServerAPIResponse<PageResponse<ProductResponse>>> searchProducts(
            ProductSearchRequest request) {
        PageResponse<ProductResponse> response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/products")
    public ResponseEntity<ServerAPIResponse<PageResponse<ProductResponse>>> getMyProducts(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            ProductSearchRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        ProductSearchRequest wrappedRequest = ProductSearchRequestWrapper.withCreatorId(request,
                userDetails.getUserId());

        PageResponse<ProductResponse> response = productSearchService.searchProducts(wrappedRequest);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/public/auctions/{auction_id}/products/{product_id}")
    public ResponseEntity<ServerAPIResponse<ProductResponse>> getProductByIdInAuction(
            @PathVariable String auction_id,
            @PathVariable String product_id) {
        // Get product via auction to ensure only approved auctions and products are accessible
        try {
            ProductResponse response = auctionService.getProductFromApprovedAuction(auction_id, product_id);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/products")
    public ResponseEntity<ServerAPIResponse<ProductResponse>> createProduct(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestBody ProductCreateRequest request) { // Dùng @RequestBody thay cho @RequestPart

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();
        ProductResponse response = productService.createProduct(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ServerAPIResponse.success(response));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ServerAPIResponse<ProductResponse>> updateProduct(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody ProductUpdateRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();
        ProductResponse response = productService.updateProduct(id, request, userId);

        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ServerAPIResponse<Void>> deleteProduct(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();

        productService.deleteProduct(id, userId);

        return ResponseEntity.ok(ServerAPIResponse.success(null));
    }

    // ========== ADMIN APPROVAL ENDPOINTS ==========

    @PostMapping("/admin/products/{id}/approve")
    public ResponseEntity<ServerAPIResponse<ProductResponse>> approveProduct(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        ProductResponse response = productService.approveProduct(id, userDetails.getUserId());
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/admin/products/{id}/reject")
    public ResponseEntity<ServerAPIResponse<ProductResponse>> rejectProduct(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        String reason = request != null ? request.get("reason") : null;
        ProductResponse response = productService.rejectProduct(id, userDetails.getUserId(), reason);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/products/pending")
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

        ProductSearchRequest request = new ProductSearchRequest(
                null, null, null, page, pageSize, null, null, null, "CHUA_DUYET");
        
        PageResponse<ProductResponse> response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/products/approved")
    public ResponseEntity<ServerAPIResponse<PageResponse<ProductResponse>>> getApprovedProducts(
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

        ProductSearchRequest request = new ProductSearchRequest(
                null, null, null, page, pageSize, null, null, null, "DA_DUYET");
        
        PageResponse<ProductResponse> response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/products/rejected")
    public ResponseEntity<ServerAPIResponse<PageResponse<ProductResponse>>> getRejectedProducts(
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

        ProductSearchRequest request = new ProductSearchRequest(
                null, null, null, page, pageSize, null, null, null, "BI_TU_CHOI");
        
        PageResponse<ProductResponse> response = productSearchService.searchProducts(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }
}