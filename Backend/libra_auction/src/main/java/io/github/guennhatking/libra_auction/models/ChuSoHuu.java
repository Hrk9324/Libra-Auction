package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;

@Entity
public class ChuSoHuu extends NguoiDung {
    public ChuSoHuu(String hoVaTen, String soDienThoai, String CCCD) {
        super(hoVaTen, soDienThoai, CCCD);
    }
    
}
