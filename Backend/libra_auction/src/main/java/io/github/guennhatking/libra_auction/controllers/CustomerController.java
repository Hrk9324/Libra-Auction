package io.github.guennhatking.libra_auction.controllers;

import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.guennhatking.libra_auction.mappers.CustomerMapper;
import io.github.guennhatking.libra_auction.models.person.Customer;
import io.github.guennhatking.libra_auction.security.JwtUserDetails;
import io.github.guennhatking.libra_auction.services.CustomerService;
import io.github.guennhatking.libra_auction.viewmodels.request.ChangePasswordRequest;
import io.github.guennhatking.libra_auction.viewmodels.response.CustomerResponse;
import io.github.guennhatking.libra_auction.viewmodels.response.ServerAPIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User", description = "User profile and account management endpoints")
public class CustomerController {
    private final CustomerService userService;
    private final CustomerMapper userMapper;

    public CustomerController(CustomerService userService) {
        this.userService = userService;
        this.userMapper = Mappers.getMapper(CustomerMapper.class);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get User Info", description = "Retrieve user profile information by user ID")
    @ApiResponse(responseCode = "200", description = "User info retrieved successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ServerAPIResponse<CustomerResponse>> getUserInfo(@PathVariable String id) {
        Optional<Customer> user = userService.findById(id);

        if (user.isPresent()) {
            CustomerResponse userResponse = userMapper.toResponse(user.get());
            return ResponseEntity.status(HttpStatus.OK).body(ServerAPIResponse.success(userResponse));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ServerAPIResponse.error("User not found"));
        }
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Change Password", description = "Change user's password. Requires current password to verify identity")
    @ApiResponse(responseCode = "200", description = "Password changed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid current password")
    @ApiResponse(responseCode = "401", description = "Unauthorized - valid JWT token required")
    public ResponseEntity<ServerAPIResponse<String>> changePassword(
            @AuthenticationPrincipal JwtUserDetails principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getUserId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(ServerAPIResponse.success("Đổi mật khẩu thành công."));
    }
}
