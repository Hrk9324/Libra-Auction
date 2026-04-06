package io.github.guennhatking.libra_auction.controllers;

import io.github.guennhatking.libra_auction.services.AuthenticationService;
import io.github.guennhatking.libra_auction.viewmodels.request.GoogleLoginRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.RefreshTokenRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninFormRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SigninRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupFormRequest;
import io.github.guennhatking.libra_auction.viewmodels.request.SignupRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.JwtResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtResponse> signup(@Valid @RequestBody SignupRequest request) throws Exception {
        JwtResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/signup", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<JwtResponse> signup(@RequestParam Map<String, String> formFields) throws Exception {
        SignupFormRequest request = new SignupFormRequest();
        request.setUsername(resolveField(formFields, "username"));
        request.setPassword(resolveField(formFields, "password"));
        request.setEmail(resolveField(formFields, "email"));
        request.setFullName(resolveField(formFields, "fullName"));

        JwtResponse response = authenticationService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@Valid @RequestBody SigninRequest request) throws Exception {
        JwtResponse response = authenticationService.signin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/signin", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<JwtResponse> signin(@RequestParam Map<String, String> formFields) throws Exception {
        SigninFormRequest request = new SigninFormRequest();
        request.setUsername(resolveField(formFields, "username"));
        request.setPassword(resolveField(formFields, "password"));

        JwtResponse response = authenticationService.signin(new SigninRequest(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<JwtResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) throws Exception {
        JwtResponse response = authenticationService.googleLogin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) throws Exception {
        String newAccessToken = authenticationService.refreshToken(request);
        return ResponseEntity.ok(new TokenResponse(newAccessToken, System.currentTimeMillis() / 1000 + 86400));
    }

    private String resolveField(Map<String, String> formFields, String fieldName) {
        String value = formFields.get(fieldName);
        if (value == null) {
            value = formFields.get('"' + fieldName + '"');
        }
        return normalizeValue(value);
    }

    private String normalizeValue(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\",")) {
            normalized = normalized.substring(1, normalized.length() - 2).trim();
        } else {
            if (normalized.length() >= 2 && normalized.startsWith("\"") && normalized.endsWith("\"")) {
                normalized = normalized.substring(1, normalized.length() - 1).trim();
            }
            if (normalized.endsWith(",")) {
                normalized = normalized.substring(0, normalized.length() - 1).trim();
            }
        }

        return normalized;
    }
}
