package io.github.guennhatking.libra_auction.models.transaction;

import io.github.guennhatking.libra_auction.enums.transaction.LoaiGiaoDich;
import io.github.guennhatking.libra_auction.models.auction.KetQuaDauGia;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class GiaoDichThanhToan extends GiaoDich {
    @ManyToOne
    private NguoiDung nguoiGui;

    @ManyToOne
    private NguoiDung nguoiNhan;

    @OneToOne
    private KetQuaDauGia ketQuaDauGia; 

    // CONSTRUCTOR
    protected GiaoDichThanhToan() {
    }

    public GiaoDichThanhToan(long soTien, NguoiDung nguoiGui, NguoiDung nguoiNhan, KetQuaDauGia ketQuaDauGia) {
        super(LoaiGiaoDich.THANH_TOAN, soTien);
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
        this.ketQuaDauGia = ketQuaDauGia;
    }

    // GETTER
    public NguoiDung getNguoiGui() {
        return nguoiGui;
    }

    public NguoiDung getNguoiNhan() {
        return nguoiNhan;
    }

    public KetQuaDauGia getKetQuaDauGia() {
        return ketQuaDauGia;
    }

    // SETTER
    public void setNguoiGui(NguoiDung nguoiGui) {
        this.nguoiGui = nguoiGui;
    }

    public void setNguoiNhan(NguoiDung nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public void setKetQuaDauGia(KetQuaDauGia ketQuaDauGia) {
        this.ketQuaDauGia = ketQuaDauGia;
    }
}