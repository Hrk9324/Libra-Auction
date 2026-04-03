package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class KetQuaDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    private PhienDauGia phienDauGia;

    @ManyToOne
    private NguoiDung nguoiThangDauGia;

    private LocalDateTime thoiGianKetThuc;
    private long giaTrungDauGia;

    // CONSTRUCTOR
    public KetQuaDauGia() {
    }

    public KetQuaDauGia(PhienDauGia phienDauGia, NguoiDung nguoiThangDauGia, long giaTrungDauGia) {
        this.phienDauGia = phienDauGia;
        this.nguoiThangDauGia = nguoiThangDauGia;
        this.giaTrungDauGia = giaTrungDauGia;
        this.thoiGianKetThuc = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public NguoiDung getNguoiThangDauGia() {
        return nguoiThangDauGia;
    }

    public LocalDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public long getGiaTrungDauGia() {
        return giaTrungDauGia;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setNguoiThangDauGia(NguoiDung nguoiThangDauGia) {
        this.nguoiThangDauGia = nguoiThangDauGia;
    }

    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public void setGiaTrungDauGia(long giaTrungDauGia) {
        this.giaTrungDauGia = giaTrungDauGia;
    }
}
