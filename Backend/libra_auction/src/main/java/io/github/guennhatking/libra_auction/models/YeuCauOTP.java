package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;

public class YeuCauOTP extends YeuCau {
    private String maOTPDaTao;
    private String maOTPNguoiDungNhap;

    public YeuCauOTP(NguoiDung nguoiYeuCau) {
        super(nguoiYeuCau, Enums.LoaiYeuCau.OTP);
    }

    public void setOTP(String maOTP) {
        this.maOTPDaTao = maOTP;
    }

    @Override
    public void kichHoat() {
        if (maOTPDaTao != null && maOTPDaTao.equals(maOTPNguoiDungNhap)) {
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.DANG_SU_DUNG;
            System.out.println("Yêu cầu OTP đã được kích hoạt thành công.");
        } else {
            System.out.println("Mã OTP không hợp lệ. Kích hoạt thất bại.");
        }
    }

    @Override
    public void suDung() {
        if (this.trangThaiYeuCau == Enums.TrangThaiYeuCau.DANG_SU_DUNG) {
            this.trangThaiYeuCau = Enums.TrangThaiYeuCau.HOAN_THANH;
            System.out.println("Yêu cầu OTP đã được sử dụng.");
        } else {
            System.out.println("Yêu cầu OTP chưa được kích hoạt hoặc đã sử dụng.");
        }
    }
}
