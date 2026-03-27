package io.github.guennhatking.libra_auction.controllers;

import java.text.ParseException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.guennhatking.libra_auction.dto.request.ApiResponse;
import io.github.guennhatking.libra_auction.dto.request.AuthenticationRequest;
import io.github.guennhatking.libra_auction.dto.request.IntrospectRequest;
import io.github.guennhatking.libra_auction.dto.request.RefreshRequest;
import io.github.guennhatking.libra_auction.dto.request.SignupRequest;
import io.github.guennhatking.libra_auction.dto.response.AuthenticationResponse;
import io.github.guennhatking.libra_auction.dto.response.IntrospectResponse;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<UserResponse> signup(@RequestBody SignupRequest request) {
        var result = authenticationService.signup(request);
        return ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/signin/password")
    AuthenticationResponse authenticate(@RequestBody AuthenticationRequest request) {
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh")
    AuthenticationResponse refresh(@RequestBody RefreshRequest request)
            throws JOSEException, ParseException, com.nimbusds.oauth2.sdk.ParseException {
        return authenticationService.refreshToken(request);
    }
}