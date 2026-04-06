package io.github.guennhatking.libra_auction.viewmodels.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class SignupFormRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    private String username;

    @Size(min = 6, message = "INVALID_PASSWORD")
    private String password;

    @Email(message = "INVALID_EMAIL")
    private String email;

    private String fullName;

    public SignupFormRequest() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}