package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request tạo thanh toán VNPay
 */
public record VNPayPaymentRequest(
        @NotBlank(message = "Mã phiên đấu giá không được để trống")
        String phienDauGiaId,

        @NotBlank(message = "Mã kết quả đấu giá không được để trống")
        String ketQuaDauGiaId
) {}
