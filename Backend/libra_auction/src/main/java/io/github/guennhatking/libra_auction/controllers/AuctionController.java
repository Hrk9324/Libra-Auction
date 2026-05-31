package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuctionSearchService;
import io.github.guennhatking.libra_auction.services.AuctionService;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.models.qa.Question;
import io.github.guennhatking.libra_auction.repositories.qa.QuestionRepository;
import io.github.guennhatking.libra_auction.enums.qa.QuestionStatus;
import io.github.guennhatking.libra_auction.utils.ParseDateTime;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionCreateRequest;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSearchRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionSearchRequestWrapper;
import io.github.guennhatking.libra_auction.viewmodels.request.AuctionUpdateRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionQuestionAnswerResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionQuestionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.AuctionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.PageResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.github.guennhatking.libra_auction.models.person.Customer;
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
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuctionController {
    private final AuctionService auctionService;
    private final AuctionSearchService searchService;
    private final CustomerService userService;
    private final QuestionRepository questionRepository;

    public AuctionController(AuctionService auctionService,
            AuctionSearchService searchService,
            CustomerService userService,
            QuestionRepository questionRepository) {
        this.auctionService = auctionService;
        this.searchService = searchService;
        this.userService = userService;
        this.questionRepository = questionRepository;
    }

    // Helper method to check if user is admin
    private boolean isAdminUser(String userId) {
        Optional<Customer> user = userService.findById(userId);
        if (user.isEmpty()) {
            return false;
        }
        return user.get().getRole() != null &&
                "ADMIN".equalsIgnoreCase(user.get().getRole().getName());
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
            @RequestParam(defaultValue = "startTime") String sortBy,
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
            @RequestParam(defaultValue = "startTime") String sortBy,
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
            @RequestParam(defaultValue = "startTime") String sortBy,
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
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> getPublicAuctionById(
            @PathVariable String id) {

        AuctionResponse response = auctionService.getAuctionById(id);

        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @GetMapping("/public/auctions/{id}/questions")
    public ResponseEntity<ServerAPIResponse<List<AuctionQuestionResponse>>> getPublicAuctionQuestions(
            @PathVariable String id) {

        List<AuctionQuestionResponse> questions = questionRepository
                .findByAuctionIdOrderByQuestionTimeAsc(id)
                .stream()
                .map(this::toQuestionResponse)
                .toList();

        return ResponseEntity.ok(ServerAPIResponse.success(questions));
    }

    @GetMapping("/auctions/{id}")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> getAuctionById(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        String userId = userDetails.getUserId();
        AuctionResponse response = auctionService.getAuctionById(id, userId);

        return ResponseEntity.ok(ServerAPIResponse.success(response));

    }

    private AuctionQuestionResponse toQuestionResponse(Question question) {
    String userName = question.getQuestioner() != null && question.getQuestioner().getFullName() != null
        && !question.getQuestioner().getFullName().isBlank()
        ? question.getQuestioner().getFullName()
        : "Anonymous";

    AuctionQuestionAnswerResponse answer = question.getQuestionStatus() == QuestionStatus.ANSWERED
        && question.getAnswerContent() != null
        && !question.getAnswerContent().isBlank()
            ? new AuctionQuestionAnswerResponse(question.getAnswerContent(), question.getAnswerTime())
            : null;

    return new AuctionQuestionResponse(
        question.getId(),
        userName,
        question.getQuestionContent(),
        question.getQuestionTime(),
        answer);
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

        AuctionResponse response = auctionService.approveAuction(id, userDetails.getUserId());
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
        AuctionResponse response = auctionService.rejectAuction(id, userDetails.getUserId(), reason);
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
                null, null, null, "PENDING_APPROVAL",
                page, pageSize, "startTime", "DESC");

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
                null, null, null, "APPROVED",
                page, pageSize, "startTime", "DESC");

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
                null, null, null, "REJECTED",
                page, pageSize, "startTime", "DESC");

        PageResponse<AuctionResponse> response = searchService.searchAuctions(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    private AuctionSearchRequest buildSearchCriteria(
            String name, String categoryId, Long priceFrom, Long priceTo, Long startingPrice,
            String timeStart, String timeEnd, String status, String approvalStatus,
            Integer page, Integer pageSize, String sortBy, String sortOrder) {

        OffsetDateTime parsedStart = ParseDateTime.parse(timeStart);
        OffsetDateTime parsedEnd = ParseDateTime.parse(timeEnd);

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
                approvalStatus,
                page,
                pageSize,
                sortBy,
                sortOrder,
                null);
    }

    @PostMapping("/admin/auctions/{id}/complete")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> completeAuction(
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

        AuctionResponse response = auctionService.completeAuction(id, userDetails.getUserId());
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/admin/auctions/{id}/fail")
    public ResponseEntity<ServerAPIResponse<AuctionResponse>> failAuction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> request) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }
        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        String reason = request != null ? request.get("reason") : null;
        if (reason == null || reason.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error("Failure reason is required"));
        }

        AuctionResponse response = auctionService.failAuction(id, userDetails.getUserId(), reason);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/admin/auctions/register-to-scheduler")
    public ResponseEntity<ServerAPIResponse<java.util.Map<String, Integer>>> registerAuctionsToScheduler(
            @AuthenticationPrincipal JwtUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        if (!isAdminUser(userDetails.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Admin role required"));
        }

        int registeredCount = auctionService.registerExistingAuctionsToRedis();
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("registeredCount", registeredCount);

        return ResponseEntity.ok(ServerAPIResponse.success(result));
    }
}
