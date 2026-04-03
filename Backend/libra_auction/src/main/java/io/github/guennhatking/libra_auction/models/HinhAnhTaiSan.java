package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class HinhAnhTaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private TaiSan taiSan;

    private int thuTuHienThi;
    private String hinhAnh;

    // CONSTRUCTOR
    protected HinhAnhTaiSan() {
    }

    public HinhAnhTaiSan(TaiSan taiSan, int thuTuHienThi, String hinhAnh) {
        this.taiSan = taiSan;
        this.thuTuHienThi = thuTuHienThi;
        this.hinhAnh = hinhAnh;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    public int getThuTuHienThi() {
        return thuTuHienThi;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }

    public void setThuTuHienThi(int thuTuHienThi) {
        this.thuTuHienThi = thuTuHienThi;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}
