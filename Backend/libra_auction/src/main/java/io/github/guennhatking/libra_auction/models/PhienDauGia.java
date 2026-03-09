package io.github.guennhatking.libra_auction.models;
import java.time.LocalDateTime;
import java.util.List;
import io.github.guennhatking.libra_auction.enums.Enums;

public class PhienDauGia {
    private String id; // string(10)
    private NguoiDung nguoiTao;
    private ThongTinPhienDauGia thongTinPhienDauGia;
    private LocalDateTime thoiGianBatDau;
    private long thoiLuong;
    private KetQuaDauGia ketQuaDauGia;
    private Enums.TrangThaiKiemDuyet trangThaiKiemDuyet; // "chưa được duyệt" | "đã duyệt"
    private List<CauHoi> danhSachCauHoi;
    private List<BanGhiPhienDauGia> lichSuDatGia;
    private List<ThongTinThamGiaDauGia> danhSachThamGia;
    private List<BanGhiBoCuocDauGia> danhSachBoCuoc;
    private Enums.LoaiDauGia loaiDauGia; // "đấu giá lên" | "đấu giá xuống" | "đấu giá kín" | "đấu giá ngược"
    private Enums.TrangThaiPhien trangThaiPhien; // "chưa bắt đầu" | "đang diễn ra" | "đã kết thúc" | "bị huỷ"
    private LocalDateTime thoiGianTao;
    private TaiSan taiSan;
    private long giaKhoiDiem;
    private long buocGiaNhoNhat;

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
}
   