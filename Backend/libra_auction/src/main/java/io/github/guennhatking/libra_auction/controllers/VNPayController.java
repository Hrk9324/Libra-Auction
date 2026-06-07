package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.VNPayService;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayDepositRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.VerifyPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.UserTransactionResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayPaymentResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayTransactionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for VNPay payment processing
 */
@RestController
@RequestMapping("/api/payments/vnpay")
public class VNPayController {
    private final VNPayService vnPayService;

    public VNPayController(VNPayService vnPayService) {
        this.vnPayService = vnPayService;
    }

    @PostMapping("/create-deposit")
    public ResponseEntity<ServerAPIResponse<VNPayPaymentResponse>> createDeposit(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody VNPayDepositRequest request,
            HttpServletRequest servletRequest) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            VNPayPaymentResponse response = vnPayService.createDeposit(request, userDetails.getUserId(),
                    servletRequest);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/create-payment")
    public ResponseEntity<ServerAPIResponse<VNPayPaymentResponse>> createPayment(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody VNPayPaymentRequest request,
            HttpServletRequest servletRequest) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            VNPayPaymentResponse response = vnPayService.createPayment(request, userDetails.getUserId(),
                    servletRequest);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/deposit/successed")
    public ResponseEntity<ServerAPIResponse<Boolean>> depositSuccessed(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody VerifyPaymentRequest request) {
        System.out.println("Received verify payment request: " + request);
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            boolean isVerified = vnPayService.depositSuccessed(request);
            return ResponseEntity.ok(ServerAPIResponse.success(isVerified));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/payment/successed")
    public ResponseEntity<ServerAPIResponse<Boolean>> paymentSuccessed(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody VerifyPaymentRequest request) {
        System.out.println("Received verify payment request: " + request);
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            boolean isVerified = vnPayService.paymentSuccessed(request);
            return ResponseEntity.ok(ServerAPIResponse.success(isVerified));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ServerAPIResponse<VNPayTransactionResponse>> getTransaction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String transactionId) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }

        try {
            VNPayTransactionResponse response = vnPayService.getTransactionStatus(transactionId);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/deposit/status/{auctionId}")
    public ResponseEntity<ServerAPIResponse<Boolean>> isDepositPaid(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String auctionId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }
        boolean paid = vnPayService.isDepositPaid(userDetails.getUserId(), auctionId);
        return ResponseEntity.ok(ServerAPIResponse.success(paid));
    }

    @GetMapping("/user/{userId}/transactions")
    public ResponseEntity<ServerAPIResponse<java.util.List<UserTransactionResponse>>> getUserTransactions(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String userId) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Authentication required"));
        }
        if (!userDetails.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ServerAPIResponse.error("Access denied"));
        }

        return ResponseEntity.ok(ServerAPIResponse.success(vnPayService.getTransactionsByUserId(userId)));
    }
}
