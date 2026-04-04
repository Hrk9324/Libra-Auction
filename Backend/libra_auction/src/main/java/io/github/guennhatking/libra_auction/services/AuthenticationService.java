package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.TaiKhoanPassword;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupFormRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleUserInfo;
import io.github.guennhatking.libra_auction.viewmodels.request.RefreshTokenRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.ImageUploadResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
public class AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final GoogleOAuthService googleOAuthService;
    private final ImageUploadService imageUploadService;

    public AuthenticationService(UserService userService,
                               TokenService tokenService,
                               PasswordService passwordService,
                               GoogleOAuthService googleOAuthService,
                               ImageUploadService imageUploadService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.googleOAuthService = googleOAuthService;
        this.imageUploadService = imageUploadService;
    }

    public JwtResponse signup(SignupRequest request) throws Exception {
        NguoiDung newUser = userService.createPasswordUser(
            request.email(),
            request.username(),
            request.password(),
            request.fullName()
        );

        return tokenService.generateTokens(newUser.getId());
    }

    public JwtResponse signup(SignupFormRequest request) throws Exception {
        String avatarUrl = uploadAvatar(request.getAnhDaiDien());

        NguoiDung newUser = userService.createPasswordUser(
            request.getEmail(),
            request.getUsername(),
            request.getPassword(),
            request.getFullName(),
            request.getSoDienThoai(),
            request.getCCCD(),
            avatarUrl
        );

        return tokenService.generateTokens(newUser.getId());
    }

    public JwtResponse signin(SigninRequest request) throws Exception {
        Optional<TaiKhoanPassword> account = userService.findPasswordAccountByUsername(request.getUsername());

        if (account.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        TaiKhoanPassword taiKhoan = account.get();
        if (!passwordService.verifyPassword(request.getPassword(), taiKhoan.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String userId = taiKhoan.getNguoiDung().getId();
        return tokenService.generateTokens(userId);
    }

    public JwtResponse googleLogin(GoogleLoginRequest request) throws Exception {
        GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(request.code());

        NguoiDung user = userService.createOAuthUser(
            userInfo.getEmail(),
            userInfo.getSub(),
            userInfo.getName(),
            userInfo.getPicture()
        );

        return tokenService.generateTokens(user.getId());
    }

    public String refreshToken(RefreshTokenRequest request) throws Exception {
        return tokenService.refreshAccessToken(request.getRefreshToken());
    }

    private String uploadAvatar(MultipartFile avatarFile) throws Exception {
        if (avatarFile == null || avatarFile.isEmpty()) {
            return null;
        }

        try {
            ImageUploadResponse uploadResponse = imageUploadService.uploadImage(avatarFile, "users/avatar");
            return uploadResponse.secureUrl();
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.warn("Avatar upload failed during signup, continuing without avatar", ex);
            return null;
        }
    }
}
