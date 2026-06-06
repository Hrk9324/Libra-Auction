package io.github.guennhatking.libra_auction.services;

import io.github.guennhatking.libra_auction.viewmodels.request.SignupFormRequest;
import io.github.guennhatking.libra_auction.models.account.AccountPassword;
import io.github.guennhatking.libra_auction.models.person.Customer;
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
    private final CustomerService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final GoogleOAuthService googleOAuthService;

    public AuthenticationService(CustomerService userService,
                               TokenService tokenService,
                               PasswordService passwordService,
                               GoogleOAuthService googleOAuthService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.googleOAuthService = googleOAuthService;
    }

    public JwtResponse signup(SignupRequest request) throws Exception {
        Customer newUser = userService.createPasswordUser(
            request.email(),
            request.username(),
            request.password(),
            request.fullName()
        );

        return tokenService.generateTokens(newUser);
    }

    public JwtResponse signup(SignupFormRequest request) throws Exception {
        Customer newUser = userService.createPasswordUser(
            request.email(),
            request.username(),
            request.password(),
            request.fullName()
        );

        return tokenService.generateTokens(newUser);
    }

    public JwtResponse signin(SigninRequest request) throws Exception {
        Optional<AccountPassword> account = userService.findPasswordAccountByUsername(request.username());

        if (account.isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        AccountPassword accountPassword = account.get();
        if (!passwordService.verifyPassword(request.password(), accountPassword.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return tokenService.generateTokens(accountPassword.getCustomer());
    }

    public JwtResponse googleLogin(GoogleLoginRequest request) throws Exception {
        GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(request.code());

        Customer user = userService.createOAuthUser(
            userInfo.email(),
            userInfo.sub(),
            userInfo.name(),
            userInfo.picture()
        );

        return tokenService.generateTokens(user);
    }

    public JwtResponse refreshToken(RefreshTokenRequest request) throws Exception {
        return tokenService.refreshTokens(request.refreshToken());
    }
}
