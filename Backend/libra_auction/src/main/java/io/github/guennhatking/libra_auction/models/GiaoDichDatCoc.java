package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class GiaoDichDatCoc extends GiaoDich {
    @ManyToOne
    private NguoiDung nguoiDatCoc;

    @ManyToOne
    private ThongTinThamGiaDauGia thongTinThamGia;

    private LocalDateTime thoiGianTraCoc;

    // CONSTRUCTOR
    protected GiaoDichDatCoc() {
    }

    public GiaoDichDatCoc(long soTien, NguoiDung nguoiDatCoc, ThongTinThamGiaDauGia thongTinThamGia) {
        super(io.github.guennhatking.libra_auction.enums.Enums.LoaiGiaoDich.DAT_COC, soTien);
        this.nguoiDatCoc = nguoiDatCoc;
        this.thongTinThamGia = thongTinThamGia;
        this.thoiGianTraCoc = null;
    }

    // GETTER
    public NguoiDung getNguoiDatCoc() {
        return nguoiDatCoc;
    }

    public ThongTinThamGiaDauGia getThongTinThamGia() {
        return thongTinThamGia;
    }

    public LocalDateTime getThoiGianTraCoc() {
        return thoiGianTraCoc;
    }

    // SETTER
    public void setNguoiDatCoc(NguoiDung nguoiDatCoc) {
        this.nguoiDatCoc = nguoiDatCoc;
    }

    public void setThongTinThamGia(ThongTinThamGiaDauGia thongTinThamGia) {
        this.thongTinThamGia = thongTinThamGia;
    }

    public void setThoiGianTraCoc(LocalDateTime thoiGianTraCoc) {
        this.thoiGianTraCoc = thoiGianTraCoc;
    }
}