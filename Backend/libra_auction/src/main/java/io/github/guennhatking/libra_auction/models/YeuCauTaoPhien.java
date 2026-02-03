package io.github.guennhatking.libra_auction.models;
import io.github.guennhatking.libra_auction.enums.Enums;

public class YeuCauTaoPhien {
    private PhienDauGia phienDauGia;
    private Enums.TrangThaiYeuCau trangThai; // "chưa kiểm duyệt" | "đã chấp nhận" | "đã từ chối"
    private String liDoTuChoi;

    public PhienDauGia getPhienDauGia() { return phienDauGia; }
    public Enums.TrangThaiYeuCau getTrangThai() { return trangThai; }
    public void chapNhanYeuCau() {}
    public void tuChoiYeuCau(String lyDo) {}

    //setter
    public void setPhienDauGia(PhienDauGia phienDauGia) { this.phienDauGia = phienDauGia; }
    public void setTrangThai(Enums.TrangThaiYeuCau trangThai) { this.trangThai = trangThai;}
    public void setLiDoTuChoi(String liDoTuChoi) { this.liDoTuChoi = liDoTuChoi; }
}
