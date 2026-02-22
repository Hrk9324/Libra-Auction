package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;

@Entity
public class ThongBaoEmail extends ThongBao {
    public ThongBaoEmail(NguoiDung nguoiNhan, String noiDung) {
        super(nguoiNhan, noiDung);
    }

    @Override
    public void guiThongBao() {
        // Logic to send email notification
        System.out.println("Gửi email đến " + nguoiNhan.getHoVaTen() + ": " + noiDung);
    }
}
