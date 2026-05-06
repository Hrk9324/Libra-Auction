package io.github.guennhatking.libra_auction.viewmodels.response;

import io.github.guennhatking.libra_auction.enums.transaction.TrangThaiVNPay;
import io.github.guennhatking.libra_auction.enums.transaction.TinhTrangGiaoDich;

/**
 * Response thông tin giao dịch VNPay
 */
public record VNPayTransactionResponse(
    String transactionId,        // ID giao dịch
    String vnpayTxnRef,         // TxnRef của VNPay
    long soTien,                // Số tiền
    String moTa,                // Mô tả
    String orderInfo,           // Thông tin đơn hàng
    TrangThaiVNPay trangThai,   // Trạng thái thanh toán
    String ngayTao,             // Ngày tạo
    String ngayCapNhat          // Ngày cập nhật
) {}
