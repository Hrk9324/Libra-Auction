package io.github.guennhatking.libra_auction.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class InvalidatedToken {
    @Id
    private String token;

    public InvalidatedToken() {}

    public InvalidatedToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
