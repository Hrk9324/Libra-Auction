package io.github.guennhatking.libra_auction.models;

public class DauGiaKin{
    
    /**
     * Kiểm tra tính hợp lệ của giá mới trong đấu giá kín
     * Yêu cầu: giá mới phải lớn hơn giá hiện tại
     * 
     * @param giaHienTai giá hiện tại
     * @param giaMoi giá mới được đề xuất
     * @return true nếu giá mới hợp lệ, false nếu không
     */
    public boolean kiemTraHopLe(long giaHienTai, long giaMoi) { 
        return giaMoi > giaHienTai;
    }

}

