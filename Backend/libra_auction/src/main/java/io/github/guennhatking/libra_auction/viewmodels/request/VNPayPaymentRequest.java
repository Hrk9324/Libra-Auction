package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Request tạo thanh toán VNPay
 */
public record VNPayPaymentRequest(
    @NotBlank(message = "Loại thanh toán không được để trống")
    String loaiThanhToan, // DAT_COC, THANH_TOAN
    
    @Positive(message = "Số tiền phải lớn hơn 0")
    long soTien,
    
    String moTa, // Mô tả thanh toán
    
    String auctionId, // ID phiên đấu giá (nếu thanh toán đặt cọc)
    
    String orderInfo
) {}
