package io.github.guennhatking.libra_auction.models.auction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import io.github.guennhatking.libra_auction.models.person.NguoiDung;
import io.github.guennhatking.libra_auction.models.transaction.GiaoDichThanhToan;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class KetQuaDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    private PhienDauGia phienDauGia;

    @ManyToOne
    private NguoiDung nguoiThangDauGia;

    @OneToOne(mappedBy = "ketQuaDauGia")
    private GiaoDichThanhToan giaoDichThanhToan;

    private OffsetDateTime thoiGianKetThuc;
    private long giaTrungDauGia;

    // CONSTRUCTOR
    public KetQuaDauGia() {
    }

    public KetQuaDauGia(PhienDauGia phienDauGia, NguoiDung nguoiThangDauGia, long giaTrungDauGia) {
        this.phienDauGia = phienDauGia;
        this.nguoiThangDauGia = nguoiThangDauGia;
        this.giaTrungDauGia = giaTrungDauGia;
        this.thoiGianKetThuc = OffsetDateTime.now(ZoneOffset.ofHours(7));
    }

    // GETTER
    public String getId() {
        return id;
    }

    public PhienDauGia getPhienDauGia() {
        return phienDauGia;
    }

    public NguoiDung getNguoiThangDauGia() {
        return nguoiThangDauGia;
    }

    public OffsetDateTime getThoiGianKetThuc() {
        return thoiGianKetThuc;
    }

    public long getGiaTrungDauGia() {
        return giaTrungDauGia;
    }

    public GiaoDichThanhToan getGiaoDichThanhToan() {
        return giaoDichThanhToan;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setPhienDauGia(PhienDauGia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public void setNguoiThangDauGia(NguoiDung nguoiThangDauGia) {
        this.nguoiThangDauGia = nguoiThangDauGia;
    }

    public void setThoiGianKetThuc(OffsetDateTime thoiGianKetThuc) {
        this.thoiGianKetThuc = thoiGianKetThuc;
    }

    public void setGiaTrungDauGia(long giaTrungDauGia) {
        this.giaTrungDauGia = giaTrungDauGia;
    }
}
