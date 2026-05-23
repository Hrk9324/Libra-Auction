package io.github.guennhatking.libra_auction.models.auction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import io.github.guennhatking.libra_auction.enums.auction.TrangThaiKiemDuyet;
import io.github.guennhatking.libra_auction.enums.auction.TrangThaiPhien;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.models.product.TaiSan;
import io.github.guennhatking.libra_auction.models.qa.CauHoi;
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

    private OffsetDateTime thoiGianBatDau;
    private long thoiLuong;

    private long tienCoc;
    private long giaKhoiDiem;
    private long khoangGia;

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
    private TrangThaiKiemDuyet trangThaiKiemDuyet;

    @Enumerated(EnumType.STRING)
    private TrangThaiPhien trangThaiPhien;

    private OffsetDateTime thoiGianTao;

    @ManyToOne
    private TaiSan taiSan;

    private long buocGiaNhoNhat;
    private long giaHienTai;

    // CONSTRUCTOR
    public PhienDauGia() {
    }

    public PhienDauGia(
            NguoiDung nguoiTao,
            TaiSan taiSan,
            OffsetDateTime thoiGianBatDau,
            long thoiLuong,
            long tienCoc,
            long giaKhoiDiem,
            long khoangGia,
            long buocGiaNhoNhat) {
        this.nguoiTao = nguoiTao;
        this.taiSan = taiSan;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiLuong = thoiLuong;
        this.tienCoc = tienCoc;
        this.giaKhoiDiem = giaKhoiDiem;
        this.khoangGia = khoangGia;
        this.buocGiaNhoNhat = buocGiaNhoNhat;
        this.thoiGianTao = OffsetDateTime.now(ZoneOffset.UTC);

        this.giaHienTai = giaKhoiDiem;
    }

    public PhienDauGia(
            NguoiDung nguoiTao,
            TaiSan taiSan,
            OffsetDateTime thoiGianBatDau,
            long thoiLuong,
            long tienCoc,
            long giaKhoiDiem,
            long khoangGia,
            long buocGiaNhoNhat,
            TrangThaiKiemDuyet trangThaiKiemDuyet,
            TrangThaiPhien trangThaiPhien,
            OffsetDateTime thoiGianTao) {
        this.nguoiTao = nguoiTao;
        this.taiSan = taiSan;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiLuong = thoiLuong;
        this.tienCoc = tienCoc;
        this.giaKhoiDiem = giaKhoiDiem;
        this.khoangGia = khoangGia;
        this.buocGiaNhoNhat = buocGiaNhoNhat;
        this.trangThaiKiemDuyet = trangThaiKiemDuyet;
        this.trangThaiPhien = trangThaiPhien;
        this.thoiGianTao = thoiGianTao != null ? thoiGianTao : OffsetDateTime.now(ZoneOffset.UTC);

        this.giaHienTai = giaKhoiDiem;
    }



    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiTao() {
        return nguoiTao;
    }

    public OffsetDateTime getThoiGianBatDau() {
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

    public TrangThaiKiemDuyet getTrangThaiKiemDuyet() {
        return trangThaiKiemDuyet;
    }

    public TrangThaiPhien getTrangThaiPhien() {
        return trangThaiPhien;
    }

    public OffsetDateTime getThoiGianTao() {
        return thoiGianTao;
    }

    public TaiSan getTaiSan() {
        return taiSan;
    }

    public long getGiaKhoiDiem() {
        return giaKhoiDiem;
    }

    public long getTienCoc() {
        return tienCoc;
    }

    public long getKhoangGia() {
        return khoangGia;
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

    public void setThoiGianBatDau(OffsetDateTime thoiGianBatDau) {
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

    public void setTrangThaiKiemDuyet(TrangThaiKiemDuyet trangThaiKiemDuyet) {
        this.trangThaiKiemDuyet = trangThaiKiemDuyet;
    }

    public void setTrangThaiPhien(TrangThaiPhien trangThaiPhien) {
        this.trangThaiPhien = trangThaiPhien;
    }

    public void setThoiGianTao(OffsetDateTime thoiGianTao) {
        this.thoiGianTao = thoiGianTao;
    }

    public void setTaiSan(TaiSan taiSan) {
        this.taiSan = taiSan;
    }

    public void setGiaKhoiDiem(long giaKhoiDiem) {
        this.giaKhoiDiem = giaKhoiDiem;
    }

    public void setTienCoc(long tienCoc) {
        this.tienCoc = tienCoc;
    }

    public void setKhoangGia(long khoangGia) {
        this.khoangGia = khoangGia;
    }

    public void setBuocGiaNhoNhat(long buocGiaNhoNhat) {
        this.buocGiaNhoNhat = buocGiaNhoNhat;
    }

    public void setGiaHienTai(long giaHienTai) {
        this.giaHienTai = giaHienTai;
    }
}
