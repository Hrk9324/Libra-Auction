package io.github.guennhatking.libra_auction.models;

public class GiaoDichThanhToan extends GiaoDich {
    private NguoiDung nguoiGui;
    private NguoiDung nguoiNhan;

    // Getters and setters
    public NguoiDung getNguoiGui() { return nguoiGui; }
    public NguoiDung getNguoiNhan() { return nguoiNhan; }
    public void setNguoiGui(NguoiDung nguoiGui) { this.nguoiGui = nguoiGui; }
    public void setNguoiNhan(NguoiDung nguoiNhan) { this.nguoiNhan = nguoiNhan; }
    
    // Other methods related to payment transactions can be added here
    //chức năng thanh toán sau khi đấu giá thành công
    public void taoGiaoDichThanhToan(String id, long soTien, NguoiDung nguoiGui, NguoiDung nguoiNhan) {
        this.setId(id);
        this.setLoaiGiaoDich(io.github.guennhatking.libra_auction.enums.Enums.LoaiGiaoDich.THANH_TOAN);
        this.setSoTien(soTien);
        this.setNgayTao(java.time.LocalDateTime.now());
        this.nguoiGui = nguoiGui;
        this.nguoiNhan = nguoiNhan;
    }
}