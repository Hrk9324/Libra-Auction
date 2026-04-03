package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String tenDanhMuc;
    private String hinhAnh;

    // CONSTRUCTOR
    public DanhMuc() {
    }

    public DanhMuc(String tenDanhMuc, String hinhAnh) {
        this.tenDanhMuc = tenDanhMuc;
        this.hinhAnh = hinhAnh;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public String getTenDanhMuc() {
        return tenDanhMuc;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setTenDanhMuc(String tenDanhMuc) {
        this.tenDanhMuc = tenDanhMuc;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
}
