package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;

@Entity
public abstract class ThongBao {
    protected NguoiDung nguoiNhan;
    protected String noiDung;
    protected LocalDateTime thoiGianGui;

    public ThongBao(NguoiDung nguoiNhan, String noiDung) {
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.thoiGianGui = LocalDateTime.now();
    }

    public abstract void guiThongBao();
}
