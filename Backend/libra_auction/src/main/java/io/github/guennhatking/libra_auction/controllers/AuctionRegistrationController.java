package io.github.guennhatking.libra_auction.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.AuctionRegistrationService;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionRegistrationCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionRegistrationResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auction-registrations")
public class AuctionRegistrationController {
    private final AuctionRegistrationService auctionRegistrationService;

    public AuctionRegistrationController(AuctionRegistrationService auctionRegistrationService) {
        this.auctionRegistrationService = auctionRegistrationService;
    }

    @GetMapping
    public ResponseEntity<ServerAPIResponse<List<AuctionRegistrationResponse>>> getAllRegistrations() {
        return ResponseEntity.ok(ServerAPIResponse.success(auctionRegistrationService.getAllRegistrations()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServerAPIResponse<AuctionRegistrationResponse>> getRegistrationById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ServerAPIResponse.success(auctionRegistrationService.getRegistrationById(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * Đăng kí phiên đấu giá (auto-extract userId từ JWT)
     */
    @PostMapping
    public ResponseEntity<ServerAPIResponse<AuctionRegistrationResponse>> registerForAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody AuctionRegistrationCreateRequest request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            String userId = userDetails.getUserId();
            AuctionRegistrationResponse response = auctionRegistrationService.registerForAuction(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED) 
                    .body(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ServerAPIResponse<Void>> deleteRegistration(@PathVariable String id) {
        auctionRegistrationService.deleteRegistration(id);
        return ResponseEntity.ok(ServerAPIResponse.success(null));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ServerAPIResponse<List<AuctionRegistrationResponse>>> getRegistrationsByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ServerAPIResponse.success(auctionRegistrationService.getRegistrationsByUserId(userId)));
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ServerAPIResponse<List<AuctionRegistrationResponse>>> getRegistrationsByAuctionId(@PathVariable String auctionId) {
        return ResponseEntity.ok(ServerAPIResponse.success(auctionRegistrationService.getRegistrationsByAuctionId(auctionId)));
    }

    @GetMapping("/user/{userId}/auction/{auctionId}")
    public ResponseEntity<ServerAPIResponse<AuctionRegistrationResponse>> getRegistrationByUserAndAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId, @PathVariable String auctionId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }
        if (!userDetails.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Access denied"));
        }
        try {
            AuctionRegistrationResponse response = auctionRegistrationService.getRegistrationByUserAndAuction(userId, auctionId);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }
}
