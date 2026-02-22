package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;

@Entity
public class NguoiThamGiaDauGia extends NguoiDung {
    public NguoiThamGiaDauGia(String hoVaTen, String soDienThoai, String CCCD) {
        super(hoVaTen, soDienThoai, CCCD);
    }    
}
