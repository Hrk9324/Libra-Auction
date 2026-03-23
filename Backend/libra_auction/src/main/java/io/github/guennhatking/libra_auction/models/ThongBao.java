package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    @ManyToOne
    @JoinColumn(name = "nguoi_nhan_id")
    protected NguoiDung nguoiNhan;

    protected String noiDung;
    protected LocalDateTime thoiGianGui;

    protected ThongBao() {
        // Constructor mặc định cho JPA
    }

    public ThongBao(NguoiDung nguoiNhan, String noiDung) {
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.thoiGianGui = LocalDateTime.now();
    }

    public abstract void guiThongBao();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public NguoiDung getNguoiNhan() {
        return nguoiNhan;
    }

    public void setNguoiNhan(NguoiDung nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }
}
