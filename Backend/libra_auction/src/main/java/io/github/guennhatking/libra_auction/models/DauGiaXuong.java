package io.github.guennhatking.libra_auction.models;

/**
 * Chiến lược đấu giá ngược (Reverse Auction)
 * Giá càng thấp càng tốt, người thắng là người đưa ra giá thấp nhất
 */
public class DauGiaXuong{
    /**
     * Kiểm tra giá mới có hợp lệ không
     * Giá mới phải thấp hơn giá hiện tại
     * @param giaHienTai giá hiện tại
     * @param giaMoi giá mới đưa ra
     * @return true nếu giá mới < giá hiện tại
     */
    public boolean kiemTraHopLe(long giaHienTai, long giaMoi) {
        return giaMoi < giaHienTai;
    }
}
