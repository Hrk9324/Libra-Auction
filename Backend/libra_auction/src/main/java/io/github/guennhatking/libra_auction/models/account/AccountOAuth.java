package io.github.guennhatking.libra_auction.models.account;

import io.github.guennhatking.libra_auction.enums.account.AccountStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "account_oauth")
public class AccountOAuth extends Account {
    private String provider;
    private String providerId;

    // CONSTRUCTOR
    protected AccountOAuth() {
    }

    public AccountOAuth(String id, String provider, String providerId) {
        super(id, AccountStatus.PENDING);
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Provider cannot be empty.");
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
