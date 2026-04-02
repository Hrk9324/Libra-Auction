package io.github.guennhatking.libra_auction.dto.response;

public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private String nguoiDungId;
    private String email;
    private String hoTen;

    public JwtResponse(String token, String nguoiDungId, String email, String hoTen) {
        this.token = token;
        this.nguoiDungId = nguoiDungId;
        this.email = email;
        this.hoTen = hoTen;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getNguoiDungId() {
        return nguoiDungId;
    }

    public String getEmail() {
        return email;
    }

    public String getHoTen() {
        return hoTen;
    }
}
