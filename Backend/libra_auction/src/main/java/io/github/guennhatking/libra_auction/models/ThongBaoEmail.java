package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;

@Entity
public class ThongBaoEmail extends ThongBao {

    // CONSTRUCTOR
    protected ThongBaoEmail() {
    }

    public ThongBaoEmail(NguoiDung nguoiNhan, String noiDung) {
        super(nguoiNhan, noiDung);
    }
}
