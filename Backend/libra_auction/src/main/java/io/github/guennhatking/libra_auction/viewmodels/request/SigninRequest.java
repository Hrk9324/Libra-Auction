package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.NotBlank;

public class SigninRequest {
    @NotBlank(message = "Username cannot be left blank.")
    private String username;

    @NotBlank(message = "Password cannot be left blank.")
    private String password;

    public SigninRequest() {
    }

    public SigninRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
