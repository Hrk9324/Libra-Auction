package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

public class KetQuaDauGia {
    private PhienDauGia phienDauGia;
    private NguoiDung nguoiThangDauGia;
    private LocalDateTime thoiGianKetThuc;
    private long giaTrungDauGia;

    public PhienDauGia getPhienDauGia() { return null; }
    public NguoiDung getNguoiThangDauGia() { return null; }
    public long getGiaTrungDauGia() { return 0; }

    //getter setter
    public void setPhienDauGia(PhienDauGia phienDauGia) { this.phienDauGia = phienDauGia; }
    public void setNguoiThangDauGia(NguoiDung nguoiThangDauGia) { this.nguoiThangDauGia = nguoiThangDauGia; }
    public void setGiaTrungDauGia(long giaTrungDauGia) { this.giaTrungDauGia = giaTrungDauGia;}
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) { this.thoiGianKetThuc = thoiGianKetThuc; }
    public LocalDateTime getThoiGianKetThuc() { return thoiGianKetThuc; }
    
    //tạo ket qua dau gia
    public void taoKetQuaDauGia(PhienDauGia phienDauGia, NguoiDung nguoiThangDauGia, long giaTrungDauGia) {
        this.phienDauGia = phienDauGia;
        this.nguoiThangDauGia = nguoiThangDauGia;
        this.giaTrungDauGia = giaTrungDauGia;
        this.thoiGianKetThuc = LocalDateTime.now();
    }
}
