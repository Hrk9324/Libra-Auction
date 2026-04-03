package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.models.NguoiDung;
import io.github.guennhatking.libra_auction.models.TaiKhoanPassword;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleUserInfo;
import io.github.guennhatking.libra_auction.viewmodels.request.RefreshTokenRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final GoogleOAuthService googleOAuthService;

    public AuthenticationService(UserService userService,
                               TokenService tokenService,
                               PasswordService passwordService,
                               GoogleOAuthService googleOAuthService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.googleOAuthService = googleOAuthService;
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
}
