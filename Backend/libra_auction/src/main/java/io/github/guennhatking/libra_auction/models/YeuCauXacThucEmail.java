package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;

public class YeuCauXacThucEmail extends YeuCau {
    private YeuCauOTP yeuCauOTP;
    public YeuCauXacThucEmail(NguoiDung nguoiYeuCau) {
        super(nguoiYeuCau, Enums.LoaiYeuCau.XAC_THUC_EMAIL);
    }

    @Override
    public void kichHoat() {
        if (yeuCauOTP != null && yeuCauOTP.trangThaiYeuCau == Enums.TrangThaiYeuCau.DANG_XU_LY) {
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.DANG_SU_DUNG;
            System.out.println("Yêu cầu xác thực email đã được kích hoạt thành công.");
        } else {
            System.out.println("Yêu cầu OTP chưa được sử dụng. Kích hoạt thất bại.");
        }
    }

    @Override
    public void suDung() {
        if (this.trangThaiYeuCau == Enums.TrangThaiYeuCau.DANG_SU_DUNG) {
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.HOAN_THANH;
            System.out.println("Yêu cầu xác thực email đã được sử dụng.");
        } else {
            System.out.println("Yêu cầu xác thực email chưa được kích hoạt hoặc đã sử dụng.");
        }
    }
}
