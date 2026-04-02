package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.dto.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.dto.JwtResponse;
import io.github.guennhatking.libra_auction.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/identity/signin")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody GoogleLoginRequest request) {

        String codeTuFrontend = request.getIdToken();
        System.out.println("Nhận được yêu cầu đăng nhập Google với Code: " + codeTuFrontend);
        
        try {
            System.out.println("Nhận được yêu cầu đăng nhập Google với ID Token: " + request.getIdToken());
            JwtResponse jwtResponse = authService.authenticateWithGoogle(request.getIdToken());
            return ResponseEntity.ok(jwtResponse);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Loi he thong: " + e.getMessage());
        }
    }
}