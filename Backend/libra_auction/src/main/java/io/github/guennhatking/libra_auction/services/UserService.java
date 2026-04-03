package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.enums.Enums;
import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.Role;
import io.github.guennhatking.libra_auction.models.TaiKhoanOAuth;
import io.github.guennhatking.libra_auction.models.TaiKhoanPassword;
import io.github.guennhatking.libra_auction.repositories.NguoiDungRepository;
import io.github.guennhatking.libra_auction.repositories.RoleRepository;
import io.github.guennhatking.libra_auction.repositories.TaiKhoanOAuthRepository;
import io.github.guennhatking.libra_auction.repositories.TaiKhoanPasswordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final NguoiDungRepository nguoiDungRepository;
    private final TaiKhoanPasswordRepository taiKhoanPasswordRepository;
    private final TaiKhoanOAuthRepository taiKhoanOAuthRepository;
    private final RoleRepository roleRepository;
    private final PasswordService passwordService;

    public UserService(NguoiDungRepository nguoiDungRepository,
                      TaiKhoanPasswordRepository taiKhoanPasswordRepository,
                      TaiKhoanOAuthRepository taiKhoanOAuthRepository,
                      RoleRepository roleRepository,
                      PasswordService passwordService) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.taiKhoanPasswordRepository = taiKhoanPasswordRepository;
        this.taiKhoanOAuthRepository = taiKhoanOAuthRepository;
        this.roleRepository = roleRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public NguoiDung createPasswordUser(String email, String username, String password, String hoVaTen) {
        if (taiKhoanPasswordRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("The username already exists");
        }

        NguoiDung user = new NguoiDung(hoVaTen, email);
        user.setTrangThaiEmail(Enums.TrangThaiEmail.CHUA_XAC_THUC);
        user.setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN);

        NguoiDung savedUser = nguoiDungRepository.save(user);

        String encodedPassword = passwordService.encodePassword(password);
        TaiKhoanPassword taiKhoan = new TaiKhoanPassword(
            UUID.randomUUID().toString(),
            username,
            encodedPassword,
            new byte[0]
        );
        taiKhoan.setNguoiDung(savedUser);
        taiKhoan.setTrangThai(Enums.TrangThaiTaiKhoan.CHO_XAC_NHAN);
        taiKhoanPasswordRepository.save(taiKhoan);

        assignDefaultRole(savedUser);

        return savedUser;
    }

    public NguoiDung createOAuthUser(String email, String googleId, String displayName, String pictureUrl) {
        Optional<NguoiDung> existingUser = nguoiDungRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        NguoiDung user = new NguoiDung(displayName, email);
        // user.setAnhDaiDien(pictureUrl);
        user.setTrangThaiEmail(Enums.TrangThaiEmail.DA_XAC_THUC);
        user.setTrangThaiTaiKhoan(Enums.TrangThaiTaiKhoan.HOAT_DONG);

        NguoiDung savedUser = nguoiDungRepository.save(user);

        TaiKhoanOAuth oauthAccount = new TaiKhoanOAuth(
            UUID.randomUUID().toString(),
            email,
            "google",
            googleId
        );
        oauthAccount.setNguoiDung(savedUser);
        oauthAccount.setTrangThai(Enums.TrangThaiTaiKhoan.HOAT_DONG);
        taiKhoanOAuthRepository.save(oauthAccount);

        assignDefaultRole(savedUser);

        return savedUser;
    }

    public Optional<NguoiDung> findByEmail(String email) {
        return nguoiDungRepository.findByEmail(email);
    }

    public Optional<NguoiDung> findById(String userId) {
        return nguoiDungRepository.findById(userId);
    }

    public Optional<TaiKhoanPassword> findPasswordAccountByUsername(String username) {
        return taiKhoanPasswordRepository.findByUsername(username);
    }

    public Optional<TaiKhoanOAuth> findOAuthAccountByProviderId(String providerId) {
        return taiKhoanOAuthRepository.findByProviderId(providerId);
    }

    private void assignDefaultRole(NguoiDung user) {
        Optional<Role> defaultRole = roleRepository.findById("USER");
        if (defaultRole.isPresent()) {
            user.setRoles(List.of(defaultRole.get()));
            nguoiDungRepository.save(user);
        }
    }
}
