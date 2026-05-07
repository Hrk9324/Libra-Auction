package io.github.guennhatking.libra_auction.viewmodels.response;

/**
 * Response từ VNPay IPN Callback
 */
public record VNPayCallbackResponse(
    int responseCode,      // 00 = thành công
    String responseMessage,
    String transactionId,  // ID giao dịch của hệ thống
    String vnpayTxnRef,   // TxnRef của VNPay
    String vnpayOrderInfo, // Order info từ VNPay
    long vnpayAmount       // Số tiền từ VNPay
) {}
