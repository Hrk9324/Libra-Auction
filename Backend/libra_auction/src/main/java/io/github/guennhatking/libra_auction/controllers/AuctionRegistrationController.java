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
import org.springframework.web.bind.annotation.ResponseStatus;
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
    public List<AuctionRegistrationResponse> getAllRegistrations() {
        return auctionRegistrationService.getAllRegistrations();
    }

    @GetMapping("/{id}")
    public AuctionRegistrationResponse getRegistrationById(@PathVariable String id) {
        return auctionRegistrationService.getRegistrationById(id);
    }

    /**
     * Đăng kí phiên đấu giá (auto-extract userId từ JWT)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ServerAPIResponse<AuctionRegistrationResponse>> registerForAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody AuctionRegistrationCreateRequest request) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Người dùng chưa đăng nhập"));
        }
        
        try {
            // Auto-extract userId từ JWT, ignore userId từ request body
            String userId = userDetails.getUserId();
            AuctionRegistrationCreateRequest updatedRequest = new AuctionRegistrationCreateRequest(
                userId, 
                request.auctionSessionId()
            );
            
            AuctionRegistrationResponse response = auctionRegistrationService.registerForAuction(updatedRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRegistration(@PathVariable String id) {
        auctionRegistrationService.deleteRegistration(id);
    }

    @GetMapping("/user/{userId}")
    public List<AuctionRegistrationResponse> getRegistrationsByUserId(@PathVariable String userId) {
        return auctionRegistrationService.getRegistrationsByUserId(userId);
    }

    @GetMapping("/auction/{auctionSessionId}")
    public List<AuctionRegistrationResponse> getRegistrationsByAuctionSessionId(@PathVariable String auctionSessionId) {
        return auctionRegistrationService.getRegistrationsByAuctionSessionId(auctionSessionId);
    }
}
