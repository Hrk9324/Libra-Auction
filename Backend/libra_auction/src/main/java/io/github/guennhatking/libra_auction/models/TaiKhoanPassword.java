package io.github.guennhatking.libra_auction.models;

import java.util.regex.Pattern;

import io.github.guennhatking.libra_auction.enums.Enums;

public class TaiKhoanPassword extends TaiKhoan {
    private String passwordHash;
    private byte[] salt;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{6,}$"
    );

    public TaiKhoanPassword(String id, String username, String passwordHash, byte[] salt) {
        super(id, Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN, username);
        this.passwordHash = passwordHash;
        this.salt = salt;
    }

    // kiểm tra mật khẩu
    public void kiemTraDinhDangMatKhau(String matKhau) {
        if (matKhau == null) {
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        }
        if (!PASSWORD_PATTERN.matcher(matKhau).matches()) {
            throw new IllegalArgumentException("Mật khẩu không hợp lệ. Mật khẩu phải chứa ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
        }
    }

    // Đổi mật khẩu
    public void doiMatKhau(String newPasswordHash, byte[] newSalt) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống.");
        }
        this.passwordHash = newPasswordHash;
        this.salt = newSalt;
    }

    // kiểm tra mật khẩu khi đăng nhập
    public void kiemTraMatKhau(String passwordHashToCheck) {
        if (!this.passwordHash.equals(passwordHashToCheck)) {
            throw new IllegalArgumentException("Mật khẩu không đúng.");
        }
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }
}
