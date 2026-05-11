package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuctionSearchService;
import io.github.guennhatking.libra_auction.services.AuctionService;
import io.github.guennhatking.libra_auction.services.UserService;
import io.github.guennhatking.libra_auction.utils.ParseDateTime;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionCreateRequest;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSearchRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSearchRequestWrapper;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuctionController {
    private final AuctionService auctionService;
    private final AuctionSearchService searchService;
    private final UserService userService;

    public AuctionController(AuctionService auctionService,
            AuctionSearchService searchService,
            UserService userService) {
        this.auctionService = auctionService;
        this.searchService = searchService;
        this.userService = userService;
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

    @GetMapping("/public/categories/{categoryId}/auctions")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getAuctionsByCategory(
            @PathVariable String categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long priceFrom,
            @RequestParam(required = false) Long priceTo,
            @RequestParam(required = false) Long startingPrice,
            @RequestParam(required = false) String timeStart,
            @RequestParam(required = false) String timeEnd,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "thoiGianBatDau") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        AuctionSearchRequest criteria = buildSearchCriteria(
                name, categoryId, priceFrom, priceTo, startingPrice,
                timeStart, timeEnd, status, null,
                page, pageSize, sortBy, sortOrder);

        PageResponse<AuctionResponse> result = searchService.searchPublicAuctions(criteria);

        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    @GetMapping("/public/auctions")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getAuctions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long priceFrom,
            @RequestParam(required = false) Long priceTo,
            @RequestParam(required = false) Long startingPrice,
            @RequestParam(required = false) String timeStart,
            @RequestParam(required = false) String timeEnd,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "thoiGianBatDau") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        AuctionSearchRequest criteria = buildSearchCriteria(
                name, null, priceFrom, priceTo, startingPrice,
                timeStart, timeEnd, status, null,
                page, pageSize, sortBy, sortOrder);

        PageResponse<AuctionResponse> result = searchService.searchPublicAuctions(criteria);

        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    // ---------------------------------------------------------------------
    // Owner‑only auctions endpoint – uses @AuthenticationPrincipal to obtain
    // the current user id from the SecurityContext (populated by JwtAuthFilter).
    // ---------------------------------------------------------------------
    @GetMapping("/auctions")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getMyAuctions(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long priceFrom,
            @RequestParam(required = false) Long priceTo,
            @RequestParam(required = false) Long startingPrice,
            @RequestParam(required = false) String timeStart,
            @RequestParam(required = false) String timeEnd,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(defaultValue = "thoiGianBatDau") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        // Build base criteria (same as public search)
        AuctionSearchRequest baseCriteria = buildSearchCriteria(
                name, null, priceFrom, priceTo, startingPrice,
                timeStart, timeEnd, status, null,
                page, pageSize, sortBy, sortOrder);

        // Ensure authentication succeeded
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }
        AuctionSearchRequest ownerCriteria = AuctionSearchRequestWrapper
                .withOwnerId(baseCriteria, userDetails.getUserId());

        PageResponse<AuctionResponse> result = searchService.searchAuctions(ownerCriteria);
        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }

    @GetMapping("/public/auctions/{id}")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> getAuctionByIdInCategory(
            @PathVariable String id) {

        AuctionResponse response = auctionService.getAuctionById(id);

        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/auctions")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> createAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody AuctionCreateRequest request) {

        // Ensure authentication succeeded
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();

        AuctionResponse response = auctionService.createAuction(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ServerAPIResponse.success(response));
    }

    @PutMapping("/auctions/{id}")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> updateAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id,
            @Valid @RequestBody AuctionUpdateRequest request) {

        // Ensure authentication succeeded
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();

        AuctionResponse response = auctionService.updateAuction(id, request, userId);

        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @DeleteMapping("/auctions/{id}")
    public ResponseEntity<ServerAPIResponse<Void>> deleteAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();

        auctionService.deleteAuction(id, userId);

        return ResponseEntity.ok(ServerAPIResponse.success(null));
    }

    // ========== ADMIN APPROVAL ENDPOINTS ==========

    @PostMapping("/admin/auctions/{id}/approve")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> approveAuction(
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

        AuctionResponse response = auctionService.approveAuctionWithProduct(id, userDetails.getUserId());
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/admin/auctions/{id}/reject")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> rejectAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id,
            @RequestBody(required = false) java.util.Map<String, String> request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        String reason = request != null ? request.get("reason") : null;
        AuctionResponse response = auctionService.rejectAuctionWithProduct(id, userDetails.getUserId(), reason);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/auctions/pending")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getPendingAuctions(
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

        AuctionSearchRequest request = buildSearchCriteria(
                null, null, null, null, null,
                null, null, null, "CHUA_DUYET",
                page, pageSize, "thoiGianBatDau", "DESC");

        PageResponse<AuctionResponse> response = searchService.searchAuctions(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/auctions/approved")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getApprovedAuctions(
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

        AuctionSearchRequest request = buildSearchCriteria(
                null, null, null, null, null,
                null, null, null, "DA_DUYET",
                page, pageSize, "thoiGianBatDau", "DESC");

        PageResponse<AuctionResponse> response = searchService.searchAuctions(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/admin/auctions/rejected")
    public ResponseEntity<ServerAPIResponse<PageResponse<AuctionResponse>>> getRejectedAuctions(
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

        AuctionSearchRequest request = buildSearchCriteria(
                null, null, null, null, null,
                null, null, null, "BI_TU_CHOI",
                page, pageSize, "thoiGianBatDau", "DESC");

        PageResponse<AuctionResponse> response = searchService.searchAuctions(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    private AuctionSearchRequest buildSearchCriteria(
            String name, String categoryId, Long priceFrom, Long priceTo, Long startingPrice,
            String timeStart, String timeEnd, String status, String trangThaiKiemDuyet,
            Integer page, Integer pageSize, String sortBy, String sortOrder) {

        // Logic parse thời gian
        OffsetDateTime parsedStart = ParseDateTime.parse(timeStart);
        OffsetDateTime parsedEnd = ParseDateTime.parse(timeEnd);

        // Gọi constructor của Record (15 THAM SỐ theo định nghĩa Record mới)
        return new AuctionSearchRequest(
                name,
                categoryId,
                priceFrom,
                priceTo,
                startingPrice,
                parsedStart,
                parsedEnd,
                null,
                status,
                trangThaiKiemDuyet,
                page,
                pageSize,
                sortBy,
                sortOrder,
                null);
    }
}
