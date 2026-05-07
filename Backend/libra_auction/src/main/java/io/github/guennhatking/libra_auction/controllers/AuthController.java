package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuthenticationService;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.RefreshTokenRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> signup(@Valid @RequestBody SignupRequest request) throws Exception {
        JwtResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/signin")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> signin(@Valid @RequestBody SigninRequest request) throws Exception {
        JwtResponse response = authenticationService.signin(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/google")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) throws Exception {
        JwtResponse response = authenticationService.googleLogin(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ServerAPIResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws Exception {
        String newAccessToken = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ServerAPIResponse.success(new TokenResponse(newAccessToken, System.currentTimeMillis() / 1000 + 86400)));
    }
}