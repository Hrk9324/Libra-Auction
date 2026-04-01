package io.github.guennhatking.libra_auction.controllers;

import java.text.ParseException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import io.github.guennhatking.libra_auction.dto.request.ApiResponse;
import io.github.guennhatking.libra_auction.dto.request.AuthenticationRequest;
import io.github.guennhatking.libra_auction.dto.request.RefreshRequest;
import io.github.guennhatking.libra_auction.dto.request.SignupRequest;
import io.github.guennhatking.libra_auction.dto.response.AuthenticationResponse;
import io.github.guennhatking.libra_auction.dto.response.UserResponse;
import io.github.guennhatking.libra_auction.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/identity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Validated
@Slf4j
public class AuthenticationController {
    AuthenticationService authenticationService;

    /**
     * Signup endpoint - Accepts JSON
     * Content-Type: application/json
     */
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> signupJson(@Valid @RequestBody SignupRequest request, BindingResult bindingResult) throws Exception {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getObjectName() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            log.error("=== JSON Validation Error ===");
            log.error("Errors: {}", errors);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                    .message("Validation failed: " + errors)
                    .build()
            );
        }
        
        ApiResponse<UserResponse> response = performSignup(request, "JSON");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Signup endpoint - Accepts Form-Data
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> signupFormData(@Valid @ModelAttribute SignupRequest request, BindingResult bindingResult) throws Exception {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            String errors = bindingResult.getAllErrors().stream()
                    .map(error -> error.getObjectName() + ": " + error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            log.error("=== Form-Data Validation Error ===");
            log.error("Errors: {}", errors);
            
            return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                    .message("Validation failed: " + errors)
                    .build()
            );
        }
        
        ApiResponse<UserResponse> response = performSignup(request, "Form-Data");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Signup endpoint - Fallback for auto-detection
     * Tries both JSON and Form-Data without strict content-type check
     */
    @PostMapping(value = "/signup")
    ResponseEntity<?> signupAuto(@RequestBody(required = false) SignupRequest jsonRequest,
                                   @ModelAttribute(binding = false) SignupRequest formRequest) throws Exception {
        SignupRequest request = (jsonRequest != null && jsonRequest.getUsername() != null) ? jsonRequest : formRequest;
        
        if (request == null || request.getUsername() == null) {
            return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                    .message("Invalid request: missing required fields")
                    .build()
            );
        }
        
        log.info("=== Signup Request (Auto-detect) ===");
        log.info("Received - username: {}, password: {}, email: {}, fullName: {}", 
            request.getUsername(), request.getPassword(), request.getEmail(), request.getFullName());

        try {
            ApiResponse<UserResponse> response = performSignup(request, "Auto-detected");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Signup error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                ApiResponse.builder()
                    .message("Signup failed: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * Common signup processing logic
     */
    private ApiResponse<UserResponse> performSignup(SignupRequest request, String contentType) throws Exception {
        log.info("=== Signup Request ({}) ===", contentType);
        log.info("Received - username: {}, password: {}, email: {}, fullName: {}", 
            request.getUsername(), request.getPassword(), request.getEmail(), request.getFullName());

        var result = authenticationService.signup(request);
        return ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/signin/password")
    AuthenticationResponse authenticate(@Valid @RequestBody AuthenticationRequest request) {
        log.info("=== Sign-In Request ===");
        log.info("Username: {}", request.getUsername());
        return authenticationService.authenticate(request);
    }

    @PostMapping("/refresh")
    AuthenticationResponse refresh(@RequestBody RefreshRequest request)
            throws JOSEException, ParseException, com.nimbusds.oauth2.sdk.ParseException {
        return authenticationService.refreshToken(request);
    }
}
