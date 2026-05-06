package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.VNPayService;
import io.github.guennhatking.libra_auction.properties.VNPayProperties;
import io.github.guennhatking.libra_auction.viewmodels.request.VNPayPaymentRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayPaymentResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.VNPayTransactionResponse;
import io.github.guennhatking.libra_auction.utils.VNPayUtil;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Controller xử lý thanh toán VNPay
 */
@RestController
@RequestMapping("/api/payments/vnpay")
public class VNPayController {

    private final VNPayService vnPayService;
    private final VNPayProperties vnPayProperties;

    public VNPayController(VNPayService vnPayService, VNPayProperties vnPayProperties) {
        this.vnPayService = vnPayService;
        this.vnPayProperties = vnPayProperties;
    }

    /**
     * Tạo yêu cầu thanh toán
     * POST /api/payments/vnpay/create
     */
    @PostMapping("/create")
    public ResponseEntity<ServerAPIResponse<VNPayPaymentResponse>> createPayment(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @Valid @RequestBody VNPayPaymentRequest request) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Người dùng chưa đăng nhập"));
        }
        
        try {
            VNPayPaymentResponse response = vnPayService.createPayment(request, userDetails.getUserId());
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * IPN Callback từ VNPay (Server-to-Server POST request)
     * VNPay gửi POST request tới endpoint này để thông báo kết quả thanh toán
     * Này là cách verify an toàn hơn, không phụ thuộc vào client
     * POST /api/payments/vnpay/ipn?vnp_ResponseCode=...&vnp_TxnRef=...&vnp_SecureHash=...
     */
    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleIPN(
            @RequestParam Map<String, String> params) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            vnPayService.handleCallback(params);
            
            String responseCode = params.get("vnp_ResponseCode");
            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("RspCode", "99");
            response.put("Message", "Confirm Fail");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Callback từ VNPay sau khi thanh toán (GET redirect - từ vnp_ReturnUrl)
     * Người dùng được redirect về URL này sau khi thanh toán
     * GET /api/payments/vnpay/callback?vnp_Amount=...&vnp_ResponseCode=...&vnp_TxnRef=...&vnp_SecureHash=...
     */
    @GetMapping("/callback")
    public ResponseEntity<ServerAPIResponse<String>> handleCallback(
            @RequestParam Map<String, String> params) {
        
        try {
            vnPayService.handleCallback(params);
            
            String responseCode = params.get("vnp_ResponseCode");
            String transactionId = params.getOrDefault("vnp_OrderInfo", "unknown");
            
            if ("00".equals(responseCode)) {
                return ResponseEntity.ok(ServerAPIResponse.success(
                        "Thanh toán thành công. Transaction: " + transactionId
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ServerAPIResponse.error(
                                "Thanh toán thất bại. Response Code: " + responseCode
                        ));
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error("Lỗi xác minh chữ ký: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin giao dịch thanh toán
     * GET /api/payments/vnpay/{transactionId}
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<ServerAPIResponse<VNPayTransactionResponse>> getTransaction(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String transactionId) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Người dùng chưa đăng nhập"));
        }
        
        try {
            VNPayTransactionResponse response = vnPayService.getTransaction(transactionId);
            return ResponseEntity.ok(ServerAPIResponse.success(response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * Hủy thanh toán
     * POST /api/payments/vnpay/{transactionId}/cancel
     */
    @PostMapping("/{transactionId}/cancel")
    public ResponseEntity<ServerAPIResponse<String>> cancelPayment(
            @AuthenticationPrincipal JwtUserDetails userDetails,
            @PathVariable String transactionId) {
        
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ServerAPIResponse.error("Người dùng chưa đăng nhập"));
        }
        
        try {
            vnPayService.cancelPayment(transactionId);
            return ResponseEntity.ok(ServerAPIResponse.success("Hủy thanh toán thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * Frontend gửi callback parameters từ VNPay return URL
     * POST /api/payments/vnpay/verify-callback
     * Body: Map của callback parameters từ VNPay
     */
    @PostMapping("/verify-callback")
    public ResponseEntity<ServerAPIResponse<String>> verifyCallback(
            @RequestParam Map<String, String> params) {
        
        try {
            vnPayService.handleCallback(params);
            String transactionId = params.values().stream()
                    .filter(v -> v.matches("[a-f0-9-]{36}"))
                    .findFirst()
                    .orElse("unknown");
            
            return ResponseEntity.ok(ServerAPIResponse.success(
                    "Xác nhận thanh toán thành công. Transaction ID: " + transactionId
            ));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error("Lỗi xác minh chữ ký: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ServerAPIResponse.error(e.getMessage()));
        }
    }

    /**
     * Helper endpoint để tính SHA512 hash cho test IPN/Callback
     * GET /api/payments/vnpay/test-hash?vnp_ResponseCode=00&vnp_TxnRef=...&vnp_Amount=...
     * Trả về hash để dùng trong vnp_SecureHash parameter
     */
    @GetMapping("/test-hash")
    public ResponseEntity<Map<String, String>> calculateTestHash(
            @RequestParam Map<String, String> params) {
        try {
            String hash = VNPayUtil.buildSecureHash(params, vnPayProperties.getHashSecret());
            Map<String, String> response = new java.util.HashMap<>();
            response.put("vnp_SecureHash", hash);
            response.put("message", "Copy vnp_SecureHash value trên vào Postman request parameter 'vnp_SecureHash'");
            response.put("tmnCode", vnPayProperties.getTmnCode());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
