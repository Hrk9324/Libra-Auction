package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class PhienDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private NguoiDung nguoiTao;

    @OneToOne(cascade = CascadeType.ALL)
    private ThongTinPhienDauGia thongTinPhienDauGia;

    private LocalDateTime thoiGianBatDau;
    private long thoiLuong;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "phienDauGia")
    private KetQuaDauGia ketQuaDauGia;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "phienDauGia")
    private List<CauHoi> danhSachCauHoi;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "phienDauGia")
    private List<BanGhiPhienDauGia> lichSuDatGia;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "phienDauGia")
    private List<ThongTinThamGiaDauGia> danhSachThamGia;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "phienDauGia")
    private List<BanGhiBoCuocDauGia> danhSachBoCuoc;

    @Enumerated(EnumType.STRING)
    private Enums.TrangThaiKiemDuyet trangThaiKiemDuyet;

    @Enumerated(EnumType.STRING)
    private Enums.LoaiDauGia loaiDauGia;

    @Enumerated(EnumType.STRING)
    private Enums.TrangThaiPhien trangThaiPhien;

    private LocalDateTime thoiGianTao;

    @ManyToOne
    private TaiSan taiSan;

    private long giaKhoiDiem;
    private long buocGiaNhoNhat;
    private long giaHienTai;

    // CONSTRUCTOR
    protected PhienDauGia() {
    }

    public PhienDauGia(NguoiDung nguoiTao, ThongTinPhienDauGia thongTinPhienDauGia, LocalDateTime thoiGianBatDau, long giaKhoiDiem, long buocGiaNhoNhat) {
        this.nguoiTao = nguoiTao;
        this.thongTinPhienDauGia = thongTinPhienDauGia;
        this.thoiGianBatDau = thoiGianBatDau;
        this.giaKhoiDiem = giaKhoiDiem;
        this.buocGiaNhoNhat = buocGiaNhoNhat;
        this.thoiGianTao = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiTao() {
        return nguoiTao;
    }

    public ThongTinPhienDauGia getThongTinPhienDauGia() {
        return thongTinPhienDauGia;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public long getThoiLuong() {
        return thoiLuong;
    }

    public KetQuaDauGia getKetQuaDauGia() {
        return ketQuaDauGia;
    }

    public List<CauHoi> getDanhSachCauHoi() {
        return danhSachCauHoi;
    }

    public List<BanGhiPhienDauGia> getLichSuDatGia() {
        return lichSuDatGia;
    }

    public List<ThongTinThamGiaDauGia> getDanhSachThamGia() {
        return danhSachThamGia;
    }

    public List<BanGhiBoCuocDauGia> getDanhSachBoCuoc() {
        return danhSachBoCuoc;
    }

    public Enums.TrangThaiKiemDuyet getTrangThaiKiemDuyet() {
        return trangThaiKiemDuyet;
    }

    public Enums.LoaiDauGia getLoaiDauGia() {
        return loaiDauGia;
    }

    public Enums.TrangThaiPhien getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public LocalDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    public long getGiaKhoiDiem() {
        return giaKhoiDiem;
    }

    public long getBuocGiaNhoNhat() {
        return buocGiaNhoNhat;
    }

    public long getGiaHienTai() {
        return giaHienTai;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiTao(NguoiDung nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public void setThongTinPhienDauGia(ThongTinPhienDauGia thongTinPhienDauGia) {
        this.thongTinPhienDauGia = thongTinPhienDauGia;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public void setThoiLuong(long thoiLuong) {
        this.thoiLuong = thoiLuong;
    }

    public void setKetQuaDauGia(KetQuaDauGia ketQuaDauGia) {
        this.ketQuaDauGia = ketQuaDauGia;
    }

    public void setDanhSachCauHoi(List<CauHoi> danhSachCauHoi) {
        this.danhSachCauHoi = danhSachCauHoi;
    }

    public void setLichSuDatGia(List<BanGhiPhienDauGia> lichSuDatGia) {
        this.lichSuDatGia = lichSuDatGia;
    }

    public void setDanhSachThamGia(List<ThongTinThamGiaDauGia> danhSachThamGia) {
        this.danhSachThamGia = danhSachThamGia;
    }

    public void setDanhSachBoCuoc(List<BanGhiBoCuocDauGia> danhSachBoCuoc) {
        this.danhSachBoCuoc = danhSachBoCuoc;
    }

    public void setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet trangThaiKiemDuyet) {
        this.trangThaiKiemDuyet = trangThaiKiemDuyet;
    }

    public void setLoaiDauGia(Enums.LoaiDauGia loaiDauGia) {
        this.loaiDauGia = loaiDauGia;
    }

    public void setTrangThaiPhien(Enums.TrangThaiPhien trangThaiPhien) {
        this.trangThaiPhien = trangThaiPhien;
    }

    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }

    public void setGiaKhoiDiem(long giaKhoiDiem) {
        this.giaKhoiDiem = giaKhoiDiem;
    }

    public void setBuocGiaNhoNhat(long buocGiaNhoNhat) {
        this.buocGiaNhoNhat = buocGiaNhoNhat;
    }

    public void setGiaHienTai(long giaHienTai) {
        this.giaHienTai = giaHienTai;
    }
}
   