package io.github.guennhatking.libra_auction.enums.transaction;

/**
 * Trạng thái thanh toán VNPay
 * Sử dụng cho cache, riêng biệt với TinhTrangGiaoDich entity
 */
public enum TrangThaiVNPay {
    DANG_THANH_TOAN,      // Đang chờ thanh toán (chuyển hướng đến VNPay)
    THANH_TOAN_THANH_CONG, // Thanh toán thành công
    THANH_TOAN_THAT_BAI,   // Thanh toán thất bại
    DA_HUY                 // Hủy thanh toán
}
