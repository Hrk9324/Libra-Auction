package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuthenticationService;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.services.EmailNotificationService;
import io.github.guennhatking.libra_auction.services.OtpService;
import io.github.guennhatking.libra_auction.viewmodels.request.ForgotPasswordRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.RefreshTokenRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.ResetPasswordRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SendEmailVerificationRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.VerifyEmailOtpRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
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
    private final CustomerService customerService;
    private final OtpService otpService;
    private final EmailNotificationService emailNotificationService;

    public AuthController(AuthenticationService authenticationService,
                          CustomerService customerService,
                          OtpService otpService,
                          EmailNotificationService emailNotificationService) {
        this.authenticationService = authenticationService;
        this.customerService = customerService;
        this.otpService = otpService;
        this.emailNotificationService = emailNotificationService;
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
    public ResponseEntity<ServerAPIResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws Exception {
        JwtResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/email/send-verification")
    public ResponseEntity<ServerAPIResponse<String>> sendEmailVerification(
            @Valid @RequestBody SendEmailVerificationRequest request) {
        String otp = otpService.generateAndStore(request.email());
        emailNotificationService.sendEmailVerificationOtp(request.email(), otp);
        return ResponseEntity.ok(ServerAPIResponse.success("OTP đã được gửi đến email " + request.email()));
    }

    @PostMapping("/email/verify")
    public ResponseEntity<ServerAPIResponse<String>> verifyEmail(
            @Valid @RequestBody VerifyEmailOtpRequest request) {
        if (!otpService.verify(request.email(), request.otp())) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn.");
        }
        customerService.markEmailVerified(request.email());
        return ResponseEntity.ok(ServerAPIResponse.success("Xác thực email thành công."));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<ServerAPIResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String otp = otpService.generateAndStore(request.email());
        emailNotificationService.sendPasswordResetOtp(request.email(), otp);
        return ResponseEntity.ok(ServerAPIResponse.success("OTP đặt lại mật khẩu đã được gửi đến email " + request.email()));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<ServerAPIResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        customerService.resetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok(ServerAPIResponse.success("Đặt lại mật khẩu thành công."));
    }
}