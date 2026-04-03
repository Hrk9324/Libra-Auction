package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

@Entity
public class YeuCauXacThucEmail extends YeuCau {
    @OneToOne
    private YeuCauOTP yeuCauOTP;

    // CONSTRUCTOR
    protected YeuCauXacThucEmail() {
    }

    public YeuCauXacThucEmail(NguoiDung nguoiYeuCau) {
        super(nguoiYeuCau, Enums.LoaiYeuCau.XAC_THUC_EMAIL);
    }

    // GETTER
    public YeuCauOTP getYeuCauOTP() {
        return yeuCauOTP;
    }

    // SETTER
    public void setYeuCauOTP(YeuCauOTP yeuCauOTP) {
        this.yeuCauOTP = yeuCauOTP;
    }
}
