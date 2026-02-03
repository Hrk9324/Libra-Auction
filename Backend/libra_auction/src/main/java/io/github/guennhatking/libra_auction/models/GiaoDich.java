package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;

public class GiaoDich {
    private String id; // string(10)
    private Enums.LoaiGiaoDich LoaiGiaoDich; // "thanh toán" | "đặt cọc"
    private long soTien;
    private java.time.LocalDateTime ngayTao;

    //getter setter    
    public String getId() { return id; }
    public Enums.LoaiGiaoDich getLoaiGiaoDich() { return LoaiGiaoDich; }
    public long getSoTien() { return soTien; }
    public java.time.LocalDateTime getNgayTao() { return ngayTao; }

    public void setId(String id) { this.id = id; }
    public void setLoaiGiaoDich(Enums.LoaiGiaoDich loaiGiaoDich) { this.LoaiGiaoDich = loaiGiaoDich; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
    public void setNgayTao(java.time.LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    //tạo chức năng giao dịch ở đây
    public void taoGiaoDich(String id, Enums.LoaiGiaoDich loaiGiaoDich, long soTien) {
        this.id = id;
        this.LoaiGiaoDich = loaiGiaoDich;
        this.soTien = soTien;
        this.ngayTao = java.time.LocalDateTime.now();
    }


}