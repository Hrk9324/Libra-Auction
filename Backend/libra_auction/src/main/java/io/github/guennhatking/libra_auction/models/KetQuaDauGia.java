package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
@Entity
public class KetQuaDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // string(10)

    @OneToOne
    @JoinColumn(name = "phien_dau_gia_id")
    private PhienDauGia phienDauGia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_thang_dau_gia_id")
    private NguoiDung nguoiThangDauGia;

    private LocalDateTime thoiGianKetThuc;
    private long giaTrungDauGia;

    public KetQuaDauGia() {
        // Constructor mặc định
    }

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
