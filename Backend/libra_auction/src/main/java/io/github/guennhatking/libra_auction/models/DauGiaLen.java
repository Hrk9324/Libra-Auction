package io.github.guennhatking.libra_auction.models;

public class DauGiaLen {
    private String id;

    private long giaKinTamThoi;
    private long giaMoiThapNhat;

    // CONSTRUCTOR
    public DauGiaLen() {
    }

    public DauGiaLen(long giaMoiThapNhat) {
        this.giaMoiThapNhat = giaMoiThapNhat;
        this.giaKinTamThoi = 0;
    }

    // GETTER
    public String getId() {
        return id;
    }

    public long getGiaKinTamThoi() {
        return giaKinTamThoi;
    }

    public long getGiaMoiThapNhat() {
        return giaMoiThapNhat;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setGiaKinTamThoi(long giaKinTamThoi) {
        this.giaKinTamThoi = giaKinTamThoi;
    }

    public void setGiaMoiThapNhat(long giaMoiThapNhat) {
        this.giaMoiThapNhat = giaMoiThapNhat;
    }
}
