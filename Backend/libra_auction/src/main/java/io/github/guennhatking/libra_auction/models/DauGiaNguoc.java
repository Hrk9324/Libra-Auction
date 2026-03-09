
package io.github.guennhatking.libra_auction.models;

public class DauGiaNguoc{
    public boolean kiemTraHopLe(long giaHienTai, long giaMoi) { 
        return giaMoi < giaHienTai; 
    }

    public long tinhGiaThapNhat(long giaKhoiDiem, long giaThoiGianHienTai) {
        return giaThoiGianHienTai;
    }

    public long tinhBuocGiamGia(long giaHienTai) {
        return Math.max(1, giaHienTai / 10);
    }

    public boolean daKetThuc(long giaHienTai, long giaThapNhat) {
        return giaHienTai <= giaThapNhat;
    }
}