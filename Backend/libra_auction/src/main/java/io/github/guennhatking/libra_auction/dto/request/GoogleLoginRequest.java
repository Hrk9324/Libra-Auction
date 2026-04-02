package io.github.guennhatking.libra_auction.dto.request;

public class GoogleLoginRequest {
    private String idToken;

    // Constructors
    public GoogleLoginRequest() {}

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    // Getters and Setters
    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}