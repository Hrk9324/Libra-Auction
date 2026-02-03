package io.github.guennhatking.libra_auction.models;

public class GiaoDichDatCoc extends GiaoDich {
    private NguoiDung nguoiDatCoc;
    private ThongTinThamGiaDauGia thongTinThamGia;
    private java.time.LocalDateTime thoiGianTraCoc;

    // Getters and setters can be added here
    public NguoiDung getNguoiDatCoc() { return nguoiDatCoc; }
    public ThongTinThamGiaDauGia getThongTinThamGia() { return thongTinThamGia; }
    public java.time.LocalDateTime getThoiGianTraCoc() { return thoiGianTraCoc; }
    public void setNguoiDatCoc(NguoiDung nguoiDatCoc) { this.nguoiDatCoc = nguoiDatCoc; }
    public void setThongTinThamGia(ThongTinThamGiaDauGia thongTinThamGia) { this.thongTinThamGia = thongTinThamGia; }
    public void setThoiGianTraCoc(java.time.LocalDateTime thoiGianTraCoc) { this.thoiGianTraCoc = thoiGianTraCoc; }
    
    //tạo chức năng đặt cọc ở đây
    public void taoGiaoDichDatCoc(String id, long soTien, NguoiDung nguoiDatCoc, ThongTinThamGiaDauGia thongTinThamGia) {
        this.setId(id);
        this.setSoTien(soTien);
        this.setNgayTao(java.time.LocalDateTime.now());
        this.nguoiDatCoc = nguoiDatCoc;
        this.thongTinThamGia = thongTinThamGia;
        this.thoiGianTraCoc = null; // Chưa trả cọc
    }
}