package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ThongBao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected String id;

    @ManyToOne
    protected NguoiDung nguoiNhan;

    protected String noiDung;
    protected LocalDateTime thoiGianGui;

    // CONSTRUCTOR
    protected ThongBao() {
    }

    public ThongBao(NguoiDung nguoiNhan, String noiDung) {
        this.nguoiNhan = nguoiNhan;
        this.noiDung = noiDung;
        this.thoiGianGui = LocalDateTime.now();
    }

    // GETTER
    public String getId() {
        return id;
    }

    public NguoiDung getNguoiNhan() {
        return nguoiNhan;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public LocalDateTime getThoiGianGui() {
        return thoiGianGui;
    }

    // SETTER
    public void setId(String id) {
        this.id = id;
    }

    public void setNguoiNhan(NguoiDung nguoiNhan) {
        this.nguoiNhan = nguoiNhan;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void setThoiGianGui(LocalDateTime thoiGianGui) {
        this.thoiGianGui = thoiGianGui;
    }
}
