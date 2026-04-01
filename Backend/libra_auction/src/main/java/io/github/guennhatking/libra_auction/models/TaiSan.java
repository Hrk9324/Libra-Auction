package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
public class TaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(mappedBy = "taiSan", cascade = CascadeType.ALL)
    private ThongTinPhienDauGia thongTinPhienDauGia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "danh_muc_id")
    private DanhMuc danhMuc;

    private String tenTaiSan;
    private Integer soLuong;
    private String moTa;

    public String getId() { return id; }
    public ThongTinPhienDauGia getThongTinPhienDauGia() { return thongTinPhienDauGia; }
    public DanhMuc getDanhMuc() { return danhMuc; }
    public String getTenTaiSan() { return tenTaiSan; }
    public int getSoLuong() { return soLuong; }
    public String getMoTa() { return moTa; }
    public void themHinhAnh(String url) {}

    //setter
    public void setThongTinPhienDauGia(ThongTinPhienDauGia thongTinPhienDauGia) { this.thongTinPhienDauGia = thongTinPhienDauGia; }
    public void setDanhMuc(DanhMuc danhMuc) { this.danhMuc = danhMuc; }
    public void setTenTaiSan(String tenTaiSan) { this.tenTaiSan = tenTaiSan; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; } 
    public void setMoTa(String moTa) { this.moTa = moTa; }



}
