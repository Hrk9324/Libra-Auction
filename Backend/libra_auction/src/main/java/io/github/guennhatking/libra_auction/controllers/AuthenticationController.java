package io.github.guennhatking.libra_auction.controllers;

import java.text.ParseException;

import org.checkerframework.checker.units.qual.C;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import io.github.guennhatking.libra_auction.dto.request.ApiResponse;
import io.github.guennhatking.libra_auction.dto.request.AuthenticationRequest;
import io.github.guennhatking.libra_auction.dto.request.RefreshRequest;
import io.github.guennhatking.libra_auction.dto.request.SignupRequest;
import io.github.guennhatking.libra_auction.dto.response.AuthenticationResponse;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;


@CrossOrigin("*")
@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    /**
     * CHỈ NHẬN JSON
     * Frontend cần gửi: axios.post(url, { username: '...', password: '...' })
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        System.out.println("=== [CONTROLLER] Nhận request Signup dạng JSON ===");
        System.out.println(">>> Dữ liệu: " + request.getUsername() + " | " + request.getEmail());

        var result = authenticationService.signup(request);

        System.out.println("=== [CONTROLLER] Xử lý thành công. Trả về kết quả cho Frontend ===");
        return ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/signin/password")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        System.out.println("=== [CONTROLLER] Đang xác thực user: " + request.getUsername());
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request)
            throws JOSEException, ParseException {
        System.out.println("=== [CONTROLLER] Đang làm mới Token ===");
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }
}