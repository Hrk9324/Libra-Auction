package io.github.guennhatking.libra_auction.models;

public class DauGiaLen{
    private long giaKinTamThoi; // Giá kín tạm thời
    private long giaMoiThapNhat; // Giá mở kín tối thiểu
    
    public DauGiaLen(long giaMoiThapNhat) {
        this.giaMoiThapNhat = giaMoiThapNhat;
        this.giaKinTamThoi = 0;
    }
}
