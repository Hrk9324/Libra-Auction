package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ThongTinPhienDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(mappedBy = "thongTinPhienDauGia")
    private PhienDauGia phienDauGia;

    @OneToOne
    private TaiSan taiSan;

    private long tienCoc;
    private long giaKhoiDiem;
    private long khoangGia;
    private String tieuDe;

    // CONSTRUCTOR
    public ThongTinPhienDauGia() {
    }

    public ThongTinPhienDauGia(long tienCoc, long giaKhoiDiem, long khoangGia, String tieuDe, TaiSan taiSan) {
        this.tienCoc = tienCoc;
        this.giaKhoiDiem = giaKhoiDiem;
        this.khoangGia = khoangGia;
        this.tieuDe = tieuDe;
        this.taiSan = taiSan;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public long getTienCoc() {
        return tienCoc;
    }

    public long getGiaKhoiDiem() {
        return giaKhoiDiem;
    }

    public long getKhoangGia() {
        return khoangGia;
    }

    public String getTieuDe() {
        return tieuDe;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setTienCoc(long tienCoc) {
        this.tienCoc = tienCoc;
    }

    public void setGiaKhoiDiem(long giaKhoiDiem) {
        this.giaKhoiDiem = giaKhoiDiem;
    }

    public void setKhoangGia(long khoangGia) {
        this.khoangGia = khoangGia;
    }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }
}
