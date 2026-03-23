package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class ThongTinPhienDauGia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id; // string(10)

    @OneToOne(mappedBy = "thongTinPhienDauGia")
    private PhienDauGia phienDauGia;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tai_san_id", unique = true)
    private TaiSan taiSan;

    private long tienCoc;
    private long giaKhoiDiem;
    private long khoangGia;
    private String tieuDe;

    public ThongTinPhienDauGia() {
        // Constructor mặc định
    }

    public PhienDauGia getPhienDauGia() { return phienDauGia; }

    public long getTienCoc() { return tienCoc; }
    public long getGiaKhoiDiem() { return giaKhoiDiem; }
    public long getKhoangGia() { return khoangGia; }
    public String getTieuDe() { return tieuDe; }
    public TaiSan getTaiSan() { return taiSan; }

    //phienDauGia
    public void setPhienDauGia(PhienDauGia phienDauGia) { this.phienDauGia = phienDauGia; }
    //tienCoc
    public void setTienCoc(long tienCoc) { this.tienCoc = tienCoc; }
    //giaKhoiDiem
    public void setGiaKhoiDiem(long giaKhoiDiem) { this.giaKhoiDiem = giaKhoiDiem; }
    //khoangGia
    public void setKhoangGia(long khoangGia) { this.khoangGia = khoangGia; }
    //tieuDe
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    //taiSan
    public void setTaiSan(TaiSan taiSan) { this.taiSan = taiSan; }
    

}
