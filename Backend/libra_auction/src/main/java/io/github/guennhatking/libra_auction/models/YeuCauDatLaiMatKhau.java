package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;

public class YeuCauDatLaiMatKhau extends YeuCau {
    private YeuCauOTP yeuCauOTP;
    private String matKhauMoi;

    public YeuCauDatLaiMatKhau(NguoiDung nguoiYeuCau) {
        super(nguoiYeuCau, Enums.LoaiYeuCau.QUEN_MAT_KHAU);
    }

    @Override
    public void kichHoat() {
        if (yeuCauOTP != null && yeuCauOTP.trangThaiYeuCau == Enums.TrangThaiYeuCau.DANG_XU_LY) {
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.DANG_SU_DUNG;
            System.out.println("Yêu cầu đặt lại mật khẩu đã được kích hoạt thành công.");
        } else {
            System.out.println("Yêu cầu OTP chưa được sử dụng. Kích hoạt thất bại.");
        }
    }

    @Override
    public void suDung() {
        if (this.trangThaiYeuCau == Enums.TrangThaiYeuCau.DANG_SU_DUNG) {
            this.nguoiDung.capNhatMatKhau(this.matKhauMoi, null);
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.HOAN_THANH;
            System.out.println("Mật khẩu đã được đặt lại thành công.");
        } else {
            System.out.println("Yêu cầu đặt lại mật khẩu chưa được kích hoạt hoặc đã sử dụng.");
        }
    }
}
