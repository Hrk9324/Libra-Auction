package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuctionSessionService;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSessionCreateRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSessionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionSessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@RestController
@RequestMapping("/api/auction-sessions")
public class AuctionSessionController {
    private final AuctionSessionService auctionSessionService;

    public AuctionSessionController(AuctionSessionService auctionSessionService) {
        this.auctionSessionService = auctionSessionService;
    }

    @GetMapping
    public List<AuctionSessionResponse> getAuctionSessions() {
        return auctionSessionService.getAuctionSessions();
    }

    @GetMapping("/{id}")
    public AuctionSessionResponse getAuctionSessionById(@PathVariable String id) {
        return auctionSessionService.getAuctionSessionById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionSessionResponse createAuctionSession(@Valid @RequestBody AuctionSessionCreateRequest request) {
        return auctionSessionService.createAuctionSession(request);
    }

    @PutMapping("/{id}")
    public AuctionSessionResponse updateAuctionSession(@PathVariable String id, @Valid @RequestBody AuctionSessionUpdateRequest request) {
        return auctionSessionService.updateAuctionSession(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuctionSession(@PathVariable String id) {
        auctionSessionService.deleteAuctionSession(id);
    }
}
