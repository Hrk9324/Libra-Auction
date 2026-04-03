package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ThuocTinhTaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private TaiSan taiSan;

    private String tenThuocTinh;
    private String giaTri;

    // CONSTRUCTOR
    public ThuocTinhTaiSan() {
    }

    public ThuocTinhTaiSan(TaiSan taiSan, String tenThuocTinh, String giaTri) {
        this.taiSan = taiSan;
        this.tenThuocTinh = tenThuocTinh;
        this.giaTri = giaTri;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    public String getTenThuocTinh() {
        return tenThuocTinh;
    }

    public String getGiaTri() {
        return giaTri;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }

    public void setTenThuocTinh(String tenThuocTinh) {
        this.tenThuocTinh = tenThuocTinh;
    }

    public void setGiaTri(String giaTri) {
        this.giaTri = giaTri;
    }
}