package io.github.guennhatking.libra_auction.models.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.auction.TrangThaiKiemDuyet;
import io.github.guennhatking.libra_auction.models.auction.PhienDauGia;
import io.github.guennhatking.libra_auction.models.person.NguoiDung;

@Entity
public class TaiSan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    private NguoiDung nguoiTao;

    @OneToOne(mappedBy = "taiSan")
    private PhienDauGia phienDauGia;

    @ManyToOne
    private DanhMuc danhMuc;

    @OneToMany(mappedBy = "taiSan")
    private List<HinhAnhTaiSan> hinhAnhTaiSanList;

    @OneToMany(mappedBy = "taiSan")
    private List<ThuocTinhTaiSan> thuocTinhTaiSanList;

    @OneToMany(mappedBy = "taiSan")
    private List<KetHopThuocTinh> ketHopThuocTinhs;

    public List<KetHopThuocTinh> getKetHopThuocTinhs() {
        return ketHopThuocTinhs;
    }

    public void setKetHopThuocTinhs(List<KetHopThuocTinh> ketHopThuocTinhs) {
        this.ketHopThuocTinhs = ketHopThuocTinhs;
    }

    private String tenTaiSan;
    private Integer soLuong;
    
    @Column(columnDefinition = "TEXT")
    private String moTa;

    @Enumerated(EnumType.STRING)
    private TrangThaiKiemDuyet trangThaiKiemDuyet;

    // CONSTRUCTOR
    public TaiSan() {
    }

    public TaiSan(String tenTaiSan, int soLuong, String moTa, DanhMuc danhMuc) {
        this.tenTaiSan = tenTaiSan;
        this.soLuong = soLuong;
        this.moTa = moTa;
        this.danhMuc = danhMuc;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiTao() {
        return nguoiTao;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public DanhMuc getDanhMuc() {
        return danhMuc;
    }

    public String getTenTaiSan() {
        return tenTaiSan;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public String getMoTa() {
        return moTa;
    }

    public TrangThaiKiemDuyet getTrangThaiKiemDuyet() {
        return trangThaiKiemDuyet;
    }

    public List<HinhAnhTaiSan> getHinhAnhTaiSanList() {
        return hinhAnhTaiSanList;
    }

    public List<ThuocTinhTaiSan> getThuocTinhTaiSanList() {
        return thuocTinhTaiSanList;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiTao(NguoiDung nguoiTao) {
        this.nguoiTao = nguoiTao;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setDanhMuc(DanhMuc danhMuc) {
        this.danhMuc = danhMuc;
    }

    public void setTenTaiSan(String tenTaiSan) {
        this.tenTaiSan = tenTaiSan;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public void setTrangThaiKiemDuyet(TrangThaiKiemDuyet trangThaiKiemDuyet) {
        this.trangThaiKiemDuyet = trangThaiKiemDuyet;
    }

    public void setHinhAnhTaiSanList(List<HinhAnhTaiSan> hinhAnhTaiSanList) {
        this.hinhAnhTaiSanList = hinhAnhTaiSanList;
    }

    public void setThuocTinhTaiSanList(List<ThuocTinhTaiSan> thuocTinhTaiSanList) {
        this.thuocTinhTaiSanList = thuocTinhTaiSanList;
    }
}
