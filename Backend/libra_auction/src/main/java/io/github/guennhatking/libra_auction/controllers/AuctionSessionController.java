package io.github.guennhatking.libra_auction.controllers;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import io.github.guennhatking.libra_auction.dto.request.AuctionSessionCreateRequest;
import io.github.guennhatking.libra_auction.dto.request.AuctionSessionUpdateRequest;
import io.github.guennhatking.libra_auction.dto.response.AuctionSessionResponse;
import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.service.AuctionSessionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auction-sessions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class AuctionSessionController {
    AuctionSessionService auctionSessionService;

    /**
     * Create a new auction session
     * POST /api/auction-sessions
     */
    @PostMapping
    public ResponseEntity<AuctionSessionResponse> createAuctionSession(
            @Valid @RequestBody AuctionSessionCreateRequest request) {
        log.info("Creating new auction session for product: {}", request.getTaiSanId());
        AuctionSessionResponse response = auctionSessionService.createAuctionSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all auction sessions
     * GET /api/auction-sessions
     */
    @GetMapping
    public ResponseEntity<List<AuctionSessionResponse>> getAllAuctionSessions() {
        log.info("Fetching all auction sessions");
        List<AuctionSessionResponse> response = auctionSessionService.getAllAuctionSessions();
        return ResponseEntity.ok(response);
    }

    /**
     * Get auction sessions by status
     * GET /api/auction-sessions/by-status/{status}
     * Example: /api/auction-sessions/by-status/CHUA_BAT_DAU
     * MUST come before /{id} to avoid routing conflicts
     */
    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<AuctionSessionResponse>> getAuctionSessionsByStatus(
            @PathVariable Enums.TrangThaiPhien status) {
        log.info("Fetching auction sessions with status: {}", status);
        List<AuctionSessionResponse> response = auctionSessionService.getAuctionSessionsByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * Get auction sessions by type
     * GET /api/auction-sessions/by-type/{type}
     * Example: /api/auction-sessions/by-type/DAU_GIA_LEN
     * MUST come before /{id} to avoid routing conflicts
     */
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<AuctionSessionResponse>> getAuctionSessionsByType(
            @PathVariable Enums.LoaiDauGia type) {
        log.info("Fetching auction sessions with type: {}", type);
        List<AuctionSessionResponse> response = auctionSessionService.getAuctionSessionsByType(type);
        return ResponseEntity.ok(response);
    }

    /**
     * Get auction session by ID
     * GET /api/auction-sessions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuctionSessionResponse> getAuctionSession(@PathVariable String id) {
        log.info("Fetching auction session with id: {}", id);
        AuctionSessionResponse response = auctionSessionService.getAuctionSession(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update auction session by ID
     * PUT /api/auction-sessions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AuctionSessionResponse> updateAuctionSession(
            @PathVariable String id,
            @Valid @RequestBody AuctionSessionUpdateRequest request) {
        log.info("Updating auction session with id: {}", id);
        AuctionSessionResponse response = auctionSessionService.updateAuctionSession(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete auction session by ID
     * DELETE /api/auction-sessions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuctionSession(@PathVariable String id) {
        log.info("Deleting auction session with id: {}", id);
        auctionSessionService.deleteAuctionSession(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Approve auction session (Admin only)
     * PATCH /api/auction-sessions/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<AuctionSessionResponse> approveAuctionSession(@PathVariable String id) {
        log.info("Approving auction session with id: {}", id);
        AuctionSessionResponse response = auctionSessionService.approveAuctionSession(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject auction session (Admin only)
     * PATCH /api/auction-sessions/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<AuctionSessionResponse> rejectAuctionSession(@PathVariable String id) {
        log.info("Rejecting auction session with id: {}", id);
        AuctionSessionResponse response = auctionSessionService.rejectAuctionSession(id);
        return ResponseEntity.ok(response);
    }
}
