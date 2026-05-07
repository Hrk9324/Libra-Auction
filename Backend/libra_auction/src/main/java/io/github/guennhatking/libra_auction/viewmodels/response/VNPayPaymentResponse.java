package io.github.guennhatking.libra_auction.viewmodels.response;

/**
 * Response từ VNPay - chứa URL để redirect người dùng thanh toán
 */
public record VNPayPaymentResponse(
    String paymentUrl,     // URL chuyển hướng sang VNPay
    String transactionId,  // ID giao dịch của hệ thống
    String vnpayTxnRef,    // TxnRef của VNPay
    long soTien,          // Số tiền thanh toán
    String orderInfo      // Thông tin đơn hàng
) {}
