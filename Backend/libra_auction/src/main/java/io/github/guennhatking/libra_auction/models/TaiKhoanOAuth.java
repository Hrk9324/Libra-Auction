package io.github.guennhatking.libra_auction.models;

import io.github.guennhatking.libra_auction.enums.Enums;
import jakarta.persistence.Entity;

@Entity
public class TaiKhoanOAuth extends TaiKhoan {
    private String provider;
    private String providerId;

    // CONSTRUCTOR
    protected TaiKhoanOAuth() {
    }

    public TaiKhoanOAuth(String id, String username, String provider, String providerId) {
        super(id, username, Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN);
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider không được để trống.");
        }
        this.provider = provider;
        this.providerId = providerId;
    }

    // GETTER
    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    // SETTER
    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }
}