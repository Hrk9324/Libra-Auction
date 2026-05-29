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
import io.github.guennhatking.libra_auction.viewmodels.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "User Sign Up", description = "Register a new user account with email, username, password and full name")
    @ApiResponse(responseCode = "200", description = "Signup successful, returns JWT tokens")
    @ApiResponse(responseCode = "400", description = "Invalid input or username already exists")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> signup(@Valid @RequestBody SignupRequest request) throws Exception {
        JwtResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/signin")
    @Operation(summary = "User Sign In", description = "Authenticate user with username and password")
    @ApiResponse(responseCode = "200", description = "Signin successful, returns JWT tokens")
    @ApiResponse(responseCode = "400", description = "Invalid username or password")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> signin(@Valid @RequestBody SigninRequest request) throws Exception {
        JwtResponse response = authenticationService.signin(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/google")
    @Operation(summary = "Google OAuth Login", description = "Login using Google OAuth 2.0 authorization code")
    @ApiResponse(responseCode = "200", description = "Google login successful, returns JWT tokens")
    @ApiResponse(responseCode = "400", description = "Invalid Google authorization code")
    public ResponseEntity<ServerAPIResponse<JwtResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) throws Exception {
        JwtResponse response = authenticationService.googleLogin(request);
        return ResponseEntity.ok(ServerAPIResponse.success(response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Access Token", description = "Get a new access token using the refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    public ResponseEntity<ServerAPIResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws Exception {
        String newAccessToken = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ServerAPIResponse.success(new TokenResponse(newAccessToken, System.currentTimeMillis() / 1000 + 86400)));
    }

    @PostMapping("/email/send-verification")
    @Operation(summary = "Send Email Verification OTP", description = "Send OTP to user's email for email verification. OTP valid for 5 minutes")
    @ApiResponse(responseCode = "200", description = "OTP sent successfully")
    @ApiResponse(responseCode = "400", description = "Email not found or invalid")
    public ResponseEntity<ServerAPIResponse<String>> sendEmailVerification(
            @Valid @RequestBody SendEmailVerificationRequest request) {
        String otp = otpService.generateAndStore(request.email());
        emailNotificationService.sendEmailVerificationOtp(request.email(), otp);
        return ResponseEntity.ok(ServerAPIResponse.success("OTP đã được gửi đến email " + request.email()));
    }

    @PostMapping("/email/verify")
    @Operation(summary = "Verify Email with OTP", description = "Verify user's email address using OTP received via email")
    @ApiResponse(responseCode = "200", description = "Email verified successfully")
    @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    public ResponseEntity<ServerAPIResponse<String>> verifyEmail(
            @Valid @RequestBody VerifyEmailOtpRequest request) {
        if (!otpService.verify(request.email(), request.otp())) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn.");
        }
        customerService.markEmailVerified(request.email());
        return ResponseEntity.ok(ServerAPIResponse.success("Xác thực email thành công."));
    }

    @PostMapping("/password/forgot")
    @Operation(summary = "Forgot Password - Send OTP", description = "Request password reset OTP to be sent to user's email")
    @ApiResponse(responseCode = "200", description = "Password reset OTP sent successfully")
    @ApiResponse(responseCode = "400", description = "Email not found")
    public ResponseEntity<ServerAPIResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String otp = otpService.generateAndStore(request.email());
        emailNotificationService.sendPasswordResetOtp(request.email(), otp);
        return ResponseEntity.ok(ServerAPIResponse.success("OTP đặt lại mật khẩu đã được gửi đến email " + request.email()));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "Reset Password", description = "Reset user password")
    @ApiResponse(responseCode = "200", description = "Password reset successfully")
    @ApiResponse(responseCode = "400", description = "User not found")
    public ResponseEntity<ServerAPIResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        customerService.resetPassword(request.email(), request.newPassword());
        return ResponseEntity.ok(ServerAPIResponse.success("Đặt lại mật khẩu thành công."));
    }
}