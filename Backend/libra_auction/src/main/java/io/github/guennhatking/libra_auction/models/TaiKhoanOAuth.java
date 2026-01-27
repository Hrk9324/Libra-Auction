package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;

public class TaiKhoanOAuth extends TaiKhoan {
    private String provider;    //goole, facebook, github,..
    private String providerId;

    public TaiKhoanOAuth(String id, String username, String provider, String providerId) {
        super(id, Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN, username);
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider không được để trống.");
        }
        this.provider = provider;
        this.providerId = providerId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }
}