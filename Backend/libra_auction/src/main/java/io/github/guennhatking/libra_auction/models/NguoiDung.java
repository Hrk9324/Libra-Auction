package io.github.guennhatking.libra_auction.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import io.github.guennhatking.libra_auction.models.Role;

import org.jspecify.annotations.Nullable;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.OneToMany;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.ManyToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NguoiDung {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "nguoiDung")
    protected List<TaiKhoan> taiKhoanLienKet;

    protected String hoVaTen;
    protected String soDienThoai;
    protected String CCCD;
    protected String anhDaiDien;
    protected String email;
    
    @ManyToMany
    private List<Role> roles;
    
    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiEmail trangThaiEmail;

    @Enumerated(EnumType.STRING)
    protected Enums.TrangThaiTaiKhoan trangThaiTaiKhoan;

    protected LocalDateTime thoiGianTao;

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getHoVaTen() {
        return hoVaTen;
    }

    public void setHoVaTen(String hoVaTen) {
        this.hoVaTen = hoVaTen;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getCCCD() {
        return CCCD;
    }

    public void setCCCD(String CCCD) {
        this.CCCD = CCCD;
    }

    public String getAnhDaiDien() {
        return anhDaiDien;
    }

    public void setAnhDaiDien(String anhDaiDien) {
        this.anhDaiDien = anhDaiDien;
    }

    public Enums.TrangThaiTaiKhoan getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public String getEmail() {
        return email;
    }

    public Enums.TrangThaiEmail getTrangThaiEmail() {
        return trangThaiEmail;
    }

    public void setTrangThaiEmail(Enums.TrangThaiEmail trangThaiEmail) {
        this.trangThaiEmail = trangThaiEmail;
    }

    public void datTrangThaiXacThucEmail(Enums.TrangThaiEmail trangThaiEmail) {
        this.trangThaiEmail = trangThaiEmail;
    }

    public void datTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<TaiKhoan> getTaiKhoanLienKet() {
        return taiKhoanLienKet;
    }

    public void setTaiKhoanLienKet(List<TaiKhoan> taiKhoanLienKet) {
        this.taiKhoanLienKet = taiKhoanLienKet;
    }

    public List<Role> getRoles() {
    return roles;
}

    public void setRoles(List<Role> roles) {
    this.roles = roles;
    }

    // cập nhật mật khẩu mới cho người dùng
    public void capNhatMatKhau(String matKhauMoiHash, byte[] salt) {
        for (TaiKhoan taiKhoan : taiKhoanLienKet) {
            if (taiKhoan instanceof TaiKhoanPassword taiKhoanPassword) {
                taiKhoanPassword.doiMatKhau(matKhauMoiHash, salt);
                return;
            }
        }
        throw new IllegalStateException("Người dùng không có phương thức đăng nhập bằng mật khẩu.");
    }

}
