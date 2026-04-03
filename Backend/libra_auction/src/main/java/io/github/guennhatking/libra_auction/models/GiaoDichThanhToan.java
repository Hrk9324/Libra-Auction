package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class GiaoDichThanhToan extends GiaoDich {
    @ManyToOne
    private NguoiDung nguoiGui;

    @ManyToOne
    private NguoiDung nguoiNhan;

    // CONSTRUCTOR
    protected GiaoDichThanhToan() {
    }

    public GiaoDichThanhToan(long soTien, NguoiDung nguoiGui, NguoiDung nguoiNhan) {
        super(io.github.guennhatking.libra_auction.enums.Enums.LoaiGiaoDich.THANH_TOAN, soTien);
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
    }

    // GETTER
    public NguoiDung getNguoiGui() {
        return nguoiGui;
    }

    public NguoiDung getNguoiNhan() {
        return nguoiNhan;
    }

    // SETTER
    public void setNguoiGui(NguoiDung nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public void setNguoiNhan(NguoiDung nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }
}