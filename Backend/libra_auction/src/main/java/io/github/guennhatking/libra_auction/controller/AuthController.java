package io.github.guennhatking.libra_auction.controller;

import io.github.guennhatking.libra_auction.dto.*;
import io.github.guennhatking.libra_auction.security.JwtTokenProvider;
import io.github.guennhatking.libra_auction.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("signin/password/")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String accessToken = authService.authenticate(request.username(), request.password());
        
        if (accessToken != null) {
            String refreshToken = tokenProvider.generateRefreshToken(request.username());
            return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken));
        }
        
        return ResponseEntity.status(401).body("Sai thông tin đăng nhập");
    }

    @PostMapping("refresh/")
    public ResponseEntity<?> refresh(@RequestBody String refreshToken) {
        if (tokenProvider.validateToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromJWT(refreshToken);
            String newAccessToken = tokenProvider.generateAccessToken(username);
            return ResponseEntity.ok(new LoginResponse(newAccessToken, refreshToken));
        }
        return ResponseEntity.status(403).body("Refresh token không hợp lệ");
    }
}