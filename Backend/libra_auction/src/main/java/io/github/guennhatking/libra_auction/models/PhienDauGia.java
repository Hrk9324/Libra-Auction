package io.github.guennhatking.libra_auction.models;
import java.time.LocalDateTime;
import java.util.List;

// import org.checkerframework.checker.units.qual.g;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
@Entity
public class PhienDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // string(10)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id")
    private NguoiDung nguoiTao;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "thong_tin_phien_dau_gia_id")
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
    private Enums.TrangThaiKiemDuyet trangThaiKiemDuyet; // "chưa được duyệt" | "đã duyệt"

    @Enumerated(EnumType.STRING)
    private Enums.LoaiDauGia loaiDauGia; // "đấu giá lên" | "đấu giá xuống" | "đấu giá kín" | "đấu giá ngược"

    @Enumerated(EnumType.STRING)
    private Enums.TrangThaiPhien trangThaiPhien; // "chưa bắt đầu" | "đang diễn ra" | "đã kết thúc" | "bị huỷ"
    private LocalDateTime thoiGianTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_san_id")
    private TaiSan taiSan;
    
    private long giaKhoiDiem;
    private long buocGiaNhoNhat;

    public PhienDauGia() {}

    //getter
    public String getId() { return id; }
    public NguoiDung getNguoiTao() { return nguoiTao; }
    public ThongTinPhienDauGia getThongTinPhienDauGia(){ return thongTinPhienDauGia; }
    public LocalDateTime getThoiGianBatDau() { return thoiGianBatDau; }
    public long getThoiLuong() { return thoiLuong; }    
    public KetQuaDauGia getKetQuaDauGia() { return ketQuaDauGia; }
    public Enums.TrangThaiKiemDuyet getTrangThaiKiemDuyet() { return trangThaiKiemDuyet; }
    public List<CauHoi> getDanhSachCauHoi() { return danhSachCauHoi; }
    public List<BanGhiPhienDauGia> getLichSuDatGia() { return lichSuDatGia; }
    public List<ThongTinThamGiaDauGia> getDanhSachThamGia() { return danhSachThamGia; }
    public List<BanGhiBoCuocDauGia> getDanhSachBoCuoc() { return danhSachBoCuoc; }
    public Enums.LoaiDauGia getLoaiDauGia() { return loaiDauGia; }
    public Enums.TrangThaiPhien getTrangThaiPhien() { return trangThaiPhien; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public TaiSan getTaiSan() { return taiSan; }
    public long getGiaKhoiDiem() { return giaKhoiDiem; }
    public long getBuocGiaNhoNhat() { return buocGiaNhoNhat; }
    //setter
    public void setId(String id) {
        this.id = id;
    }
    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }
    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau2) {
        this.thoiGianBatDau = thoiGianBatDau2;
    }
    public void setThoiGianKetThuc(LocalDateTime thoiGianKetThuc) {
        this.thoiLuong = thoiGianKetThuc.getMinute() - this.thoiGianBatDau.getMinute();
    }
    public void setGiaKhoiDiem(long giaKhoiDiem) {
        this.giaKhoiDiem = giaKhoiDiem;
    }
    public void setBuocGiaNhoNhat(long buocGiaNhoNhat) {
       this.buocGiaNhoNhat = buocGiaNhoNhat;
    }
    public void setNguoiTao(NguoiDung nguoiTao) {
        this.nguoiTao = nguoiTao;
    }
    public void setThongTinPhienDauGia(ThongTinPhienDauGia thongTinPhienDauGia) {
        this.thongTinPhienDauGia = thongTinPhienDauGia;
    }
    public void setThoiLuong(long thoiLuong) {
        this.thoiLuong = thoiLuong;
    }
    public void setLoaiDauGia(Enums.LoaiDauGia loaiDauGia) {
        this.loaiDauGia = loaiDauGia;
    }
    public void setTrangThaiPhien(Enums.TrangThaiPhien trangThaiPhien) {
        this.trangThaiPhien = trangThaiPhien;
    }
    public void setTrangThaiKiemDuyet(Enums.TrangThaiKiemDuyet trangThaiKiemDuyet) {
        this.trangThaiKiemDuyet = trangThaiKiemDuyet;
    }
    public void setThoiGianTao(LocalDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }
}
   