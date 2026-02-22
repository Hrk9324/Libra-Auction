package io.github.guennhatking.libra_auction.models;

import java.util.ArrayList;
import java.util.List;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;

@Entity
public class QuanTriVien {
    private TaiKhoan taiKhoan;
    private List<NguoiDung> danhSachNguoiDung;
    // private List<PhienDauGia> danhSachPhienDauGia;

    public QuanTriVien(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.danhSachNguoiDung = new ArrayList<>();
        // this.danhSachPhienDauGia = new ArrayList<>();
    }

    public void duyetNguoiDung(NguoiDung nguoiDung) {
        // Logic to approve user
        if (nguoiDung != null && nguoiDung.getTaiKhoanHienTai() != null) {

            if (nguoiDung.getTaiKhoanHienTai().getTrangThai() == Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN) {
                TaiKhoan taiKhoanHienTai = nguoiDung.getTaiKhoanHienTai();
            
                taiKhoanHienTai.setTrangThai(Enums.TrangThaiTaiKhoan.HOAT_DONG);
                System.out.println( "Người dùng " + nguoiDung.getHoVaTen() + " đã được duyệt." );
            }
        } else {
            System.out.println( "Người dùng không hợp lệ hoặc đã được duyệt." );
        }
    }

    // public void duyetPhienDauGia(PhienDauGia phienDauGia) {
    //     // Logic to manage auction session
    // }

    public void khoaTaiKhoan(NguoiDung nguoiDung) {
        // Logic to lock account
        if (nguoiDung != null && nguoiDung.getTaiKhoanHienTai() != null) {

            TaiKhoan taiKhoanHienTai = nguoiDung.getTaiKhoanHienTai();
        
            taiKhoanHienTai.setTrangThai(Enums.TrangThaiTaiKhoan.KHOA);
            System.out.println( "Người dùng " + nguoiDung.getHoVaTen() + " đã bị khóa." );
        } else {
            System.out.println( "Người dùng không hợp lệ." );
        }
    }

    public void moKhoaTaiKhoan(NguoiDung nguoiDung) {
        // Logic to unlock account
        if (nguoiDung != null && nguoiDung.getTaiKhoanHienTai() != null) {

            if( nguoiDung.getTaiKhoanHienTai().getTrangThai() != Enums.TrangThaiTaiKhoan.KHOA) {
                TaiKhoan taiKhoanHienTai = nguoiDung.getTaiKhoanHienTai();
        
                taiKhoanHienTai.setTrangThai(Enums.TrangThaiTaiKhoan.HOAT_DONG);
                System.out.println( "Người dùng " + nguoiDung.getHoVaTen() + " đã được mở khóa." );
            }
        } else {
            System.out.println( "Người dùng không hợp lệ." );
        }
    }

    public List<NguoiDung> getDanhSachNguoiDung() {
        return danhSachNguoiDung;
    }
}